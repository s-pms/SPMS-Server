package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.helper.CookieHelper;
import cn.hamm.airpower.helper.EmailHelper;
import cn.hamm.airpower.model.Sort;
import cn.hamm.airpower.util.*;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
import cn.hamm.spms.module.personnel.department.DepartmentService;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.system.config.ConfigService;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.mail.MessagingException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static cn.hamm.airpower.config.Constant.*;
import static cn.hamm.airpower.exception.ServiceError.*;
import static cn.hamm.spms.common.exception.CustomError.*;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class UserService extends BaseService<UserEntity, UserRepository> {
    /**
     * <h3>密码盐的长度</h3>
     */
    public static final int PASSWORD_SALT_LENGTH = 4;

    /**
     * <h3>Code缓存秒数</h3>
     */
    private static final int CACHE_CODE_EXPIRE_SECOND = DateTimeUtil.SECOND_PER_MINUTE * 5;

    /**
     * <h3>缓存房间用户</h3>
     */
    private final String CACHE_ROOM_KEY = "ROOM_USER_";


    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ConfigService configService;


    @Autowired
    private EmailHelper emailHelper;

    @Autowired
    private CookieHelper cookieHelper;

    /**
     * <h3>获取新的密码盐</h3>
     *
     * @return 密码盐
     */
    private static @NotNull String getNewSalt() {
        return RandomUtil.randomNumbers(6);
    }

    /**
     * <h3>获取短信验证码的缓存key</h3>
     *
     * @param phone 手机号
     * @return 缓存Key
     */
    @Contract(pure = true)
    private static @NotNull String getPhoneCodeCacheKey(String phone) {
        return "sms_code_" + phone;
    }

    /**
     * <h3>获取邮箱验证码的缓存key</h3>
     *
     * @param email 邮箱
     * @return 缓存Key
     */
    @Contract(pure = true)
    private static @NotNull String getEmailCacheKey(String email) {
        return "email_code_" + email;
    }

    /**
     * <h3>获取Cookie的缓存key</h3>
     *
     * @param cookie Cookie
     * @return 缓存Key
     */
    @Contract(pure = true)
    private static @NotNull String getCookieCodeKey(String cookie) {
        return "cookie_code_" + cookie;
    }

    /**
     * <h3>获取登录用户的菜单列表</h3>
     *
     * @param userId 用户id
     * @return 菜单树列表
     */
    public List<MenuEntity> getMenuListByUserId(long userId) {
        UserEntity user = get(userId);
        if (user.isRootUser()) {
            return TreeUtil.buildTreeList(
                    Services.getMenuService().filter(new MenuEntity(), new Sort().setField("orderNo"))
            );
        }
        List<MenuEntity> menuList = new ArrayList<>();
        user.getRoleList().forEach(role -> role.getMenuList().forEach(menu -> {
            boolean isExist = menuList.stream()
                    .anyMatch(existMenu -> Objects.equals(menu.getId(), existMenu.getId()));
            if (!isExist) {
                menuList.add(menu);
            }
        }));
        return TreeUtil.buildTreeList(menuList);
    }

    /**
     * <h3>获取登录用户的权限列表</h3>
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<PermissionEntity> getPermissionListByUserId(long userId) {
        UserEntity user = get(userId);
        if (user.isRootUser()) {
            return Services.getPermissionService().getList(null);
        }
        List<PermissionEntity> permissionList = new ArrayList<>();
        user.getRoleList().forEach(roleEntity -> roleEntity.getPermissionList().forEach(permission -> {
            boolean isExist = permissionList.stream()
                    .anyMatch(existPermission -> Objects.equals(permission.getId(), existPermission.getId()));
            if (!isExist) {
                permissionList.add(permission);
            }
        }));
        return permissionList;
    }

    /**
     * <h3>修改密码</h3>
     *
     * @param user 用户信息
     */
    public void modifyMyPassword(@NotNull UserEntity user) {
        UserEntity existUser = get(user.getId());

        // 判断原始密码
        String oldPassword = user.getOldPassword();
        PARAM_INVALID.whenNotEqualsIgnoreCase(
                PasswordUtil.encode(oldPassword, existUser.getSalt()),
                existUser.getPassword(),
                "原密码输入错误，修改密码失败"
        );
        String salt = RandomUtil.randomString();
        existUser.setSalt(salt);
        existUser.setPassword(PasswordUtil.encode(user.getPassword(), salt));
        update(existUser);
    }

    /**
     * <h3>重置密码</h3>
     *
     * @param user 用户实体
     */
    public void resetMyPassword(@NotNull UserEntity user) {
        String code = null;
        UserEntity existUser = null;
        if (StringUtils.hasText(user.getPhone())) {
            existUser = repository.getByPhone(user.getPhone());
            code = getSmsCode(user.getPhone());
        } else if (StringUtils.hasText(user.getEmail())) {
            existUser = repository.getByEmail(user.getEmail());
            code = getEmailCode(user.getEmail());
        } else {
            PARAM_MISSING.show("请传入邮箱或手机号");
        }
        PARAM_INVALID.whenNotEqualsIgnoreCase(code, user.getCode(), "验证码不正确，请重新获取");
        PARAM_INVALID.whenNull(existUser, "重置密码失败，用户信息异常");

        // 验证通过 开始重置密码和盐
        String salt = RandomUtil.randomString(PASSWORD_SALT_LENGTH);
        existUser.setSalt(salt);
        existUser.setPassword(PasswordUtil.encode(user.getPassword(), salt));
        if (StringUtils.hasText(user.getEmail())) {
            redisHelper.del(getEmailCacheKey(user.getEmail()));
        }
        if (StringUtils.hasText(user.getPhone())) {
            redisHelper.del(getPhoneCodeCacheKey(user.getPhone()));
        }
        update(existUser);
    }

    /**
     * <h3>发送邮箱验证码</h3>
     *
     * @param email 邮箱
     */
    public void sendMail(String email) throws MessagingException {
        EMAIL_SEND_BUSY.when(redisHelper.hasKey(getEmailCacheKey(email)));
        String code = getNewSalt();
        redisHelper.set(getEmailCacheKey(email), code, CACHE_CODE_EXPIRE_SECOND);
        emailHelper.sendCode(email, "你收到一个邮箱验证码", code, appConfig.getProjectName());
    }

    /**
     * <h3>发送短信验证码</h3>
     */
    public void sendSms(String phone) {
        SMS_SEND_BUSY.when(redisHelper.hasKey(getPhoneCodeCacheKey(phone)));
        String code = getNewSalt();
        redisHelper.set(getPhoneCodeCacheKey(phone), code, CACHE_CODE_EXPIRE_SECOND);
        log.info("短信验证码：{}", code);
        //todo 发送验证码
    }

    /**
     * <h3>登录并设置Cookie</h3>
     *
     * @param response 响应
     * @param user     用户
     * @return AccessToken
     */
    public final String loginWithCookieAndResponse(@NotNull HttpServletResponse response, @NotNull UserEntity user) {
        // 创建AccessToken
        String accessToken = createAccessToken(user.getId());

        // 存储Cookies
        String cookieString = RandomUtil.randomString();
        saveCookie(user.getId(), cookieString);
        Cookie cookie = cookieHelper.getAuthorizeCookie(cookieString);
        cookie.setHttpOnly(false);
        cookie.setPath(STRING_SLASH);
        response.addCookie(cookie);
        return accessToken;
    }

    /**
     * <h3>存储Cookie</h3>
     *
     * @param userId UserId
     * @param cookie Cookie
     */
    public void saveCookie(Long userId, String cookie) {
        redisHelper.set(getCookieCodeKey(cookie), userId, DateTimeUtil.SECOND_PER_DAY);
    }

    /**
     * <h3>通过Cookie获取一个用户</h3>
     *
     * @param cookie Cookie
     * @return UserId
     */
    public Long getUserIdByCookie(String cookie) {
        Object userId = redisHelper.get(getCookieCodeKey(cookie));
        if (Objects.isNull(userId)) {
            return null;
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * <h3>ID+密码 账号+密码</h3>
     *
     * @param user 用户实体
     * @return 登录成功的用户
     */
    public UserEntity login(@NotNull UserEntity user) {
        UserEntity existUser = null;
        if (Objects.nonNull(user.getId())) {
            // ID登录
            existUser = getMaybeNull(user.getId());
        } else if (Objects.nonNull(user.getEmail())) {
            // 邮箱登录
            existUser = repository.getByEmail(user.getEmail());
        } else {
            PARAM_INVALID.show("ID或邮箱不能为空，请确认是否传入");
        }
        USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNull(existUser);
        // 将用户传入的密码加密与数据库存储匹配
        String encodePassword = PasswordUtil.encode(user.getPassword(), existUser.getSalt());
        USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNotEqualsIgnoreCase(encodePassword, existUser.getPassword());
        return existUser;
    }

    /**
     * <h3>邮箱验证码登录</h3>
     *
     * @param user 用户实体
     * @return 登录成功的用户
     */
    public UserEntity loginViaEmail(@NotNull UserEntity user) {
        String code = getEmailCode(user.getEmail());
        PARAM_INVALID.whenNotEquals(code, user.getCode(), "邮箱验证码不正确");
        UserEntity existUser = repository.getByEmail(user.getEmail());
        ConfigEntity configuration = configService.get(ConfigFlag.AUTO_REGISTER_EMAIL_LOGIN);
        if (configuration.booleanConfig()) {
            // 注册一个用户
            long userId = registerUserViaEmail(user.getEmail());
            existUser = get(userId);
        }
        PARAM_INVALID.whenNull(existUser, "登录的邮箱账户不存在");
        return existUser;
    }

    /**
     * <h3>邮箱注册</h3>
     *
     * @param email 邮箱
     * @return 注册的用户ID
     */
    public long registerUserViaEmail(@NotNull String email) {
        return registerUserViaEmail(email, RandomUtil.randomString());
    }

    /**
     * <h3>邮箱注册</h3>
     *
     * @param email    邮箱
     * @param password 密码
     * @return 注册的用户ID
     */
    public long registerUserViaEmail(@NotNull String email, String password) {
        // 昵称默认为邮箱账号 @ 前面的
        String nickname = email.split(STRING_AT)[0];
        String salt = RandomUtil.randomString(PASSWORD_SALT_LENGTH);
        UserEntity user = new UserEntity().setPassword(PasswordUtil.encode(password, salt))
                .setSalt(salt)
                .setNickname(nickname);
        return add(user);
    }

    /**
     * <h3>创建AccessToken</h3>
     *
     * @param userId 用户ID
     * @return AccessToken
     */
    public String createAccessToken(long userId) {
        return AccessTokenUtil.create().setPayloadId(userId, serviceConfig.getAuthorizeExpireSecond()).build(serviceConfig.getAccessTokenSecret());
    }

    /**
     * <h3>获取指定邮箱缓存的验证码</h3>
     *
     * @param email 邮箱
     * @return 验证码
     */
    private String getEmailCode(String email) {
        Object code = redisHelper.get(getEmailCacheKey(email));
        return Objects.isNull(code) ? STRING_EMPTY : code.toString();
    }

    /**
     * <h3>获取指定手机缓存的验证码</h3>
     *
     * @param phone 手机
     * @return 验证码
     */
    private String getSmsCode(String phone) {
        Object code = redisHelper.get(getPhoneCodeCacheKey(phone));
        return Objects.isNull(code) ? STRING_EMPTY : code.toString();
    }

    @Override
    protected void beforeDelete(long id) {
        UserEntity entity = get(id);
        FORBIDDEN_DELETE.when(entity.isRootUser(), "系统内置用户无法被删除!");
    }

    @Override
    protected @NotNull UserEntity beforeAdd(@NotNull UserEntity user) {
        UserEntity existUser = repository.getByEmail(user.getEmail());
        FORBIDDEN_EXIST.whenNotNull(existUser, "邮箱已经存在，请勿重复添加用户");
        if (!StringUtils.hasLength(user.getPassword())) {
            // 创建时没有设置密码的话 随机一个密码
            String salt = RandomUtil.randomString(PASSWORD_SALT_LENGTH);
            user.setPassword(PasswordUtil.encode("Aa123456", salt));
            user.setSalt(salt);
        }
        return user;
    }

    @Override
    protected void beforeDisable(long id) {
        UserEntity existUser = get(id);
        FORBIDDEN_DISABLED_NOT_ALLOWED.when(existUser.isRootUser(), "系统内置用户无法被禁用!");
    }

    @Override
    protected @NotNull List<Predicate> addSearchPredicate(@NotNull Root<UserEntity> root, @NotNull CriteriaBuilder builder, @NotNull UserEntity search) {
        Long departmentId = search.getDepartmentId();
        if (Objects.isNull(departmentId)) {
            return new ArrayList<>();
        }
        List<Predicate> predicateList = new ArrayList<>();
        Set<Long> departmentIdList = getDepartmentList(departmentId);
        if (!departmentIdList.isEmpty()) {
            Join<UserEntity, DepartmentEntity> departmentJoin = root.join("departmentList");
            Predicate inPredicate = departmentJoin.get(STRING_ID).in(departmentIdList);
            predicateList.add(inPredicate);
        }
        return predicateList;
    }

    @Contract("_ -> new")
    private @NotNull Set<Long> getDepartmentList(long parentId) {
        Set<Long> departmentList = new HashSet<>();
        getDepartmentList(parentId, departmentList);
        return departmentList;
    }

    private void getDepartmentList(long parentId, @NotNull Set<Long> departmentIds) {
        DepartmentService departmentService = Services.getDepartmentService();
        DepartmentEntity parent = departmentService.get(parentId);
        departmentIds.add(parent.getId());
        List<DepartmentEntity> children = departmentService.filter(new DepartmentEntity().setParentId(parent.getId()));
        children.forEach(child -> getDepartmentList(child.getId(), departmentIds));
    }

    /**
     * <h3>获取当前用户所在的房间ID</h3>
     *
     * @param userId 用户ID
     * @return 房间ID
     */
    public long getCurrentRoomId(long userId) {
        Object data = redisHelper.get(CACHE_ROOM_KEY + userId);
        if (Objects.isNull(data)) {
            return appConfig.getDefaultRoomId();
        }
        return Integer.parseInt(data.toString());
    }

    /**
     * <h3>保存当前用户所在的房间ID</h3>
     *
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    public void saveCurrentRoomId(long userId, long roomId) {
        redisHelper.set(CACHE_ROOM_KEY + userId, roomId, DateTimeUtil.SECOND_PER_DAY * 30);
    }
}
