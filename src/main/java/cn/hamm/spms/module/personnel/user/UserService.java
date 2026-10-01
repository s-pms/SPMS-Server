package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.api.config.ApiConfig;
import cn.hamm.airpower.cookie.CookieHelper;
import cn.hamm.airpower.core.AccessTokenUtil;
import cn.hamm.airpower.core.DateTimeUtil;
import cn.hamm.airpower.core.RandomUtil;
import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.core.exception.ServiceException;
import cn.hamm.airpower.curd.base.CurdEntity;
import cn.hamm.airpower.curd.config.AccessConfig;
import cn.hamm.airpower.curd.model.query.Sort;
import cn.hamm.airpower.curd.permission.PermissionUtil;
import cn.hamm.airpower.email.helper.EmailHelper;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.AppConfig;
import cn.hamm.spms.module.personnel.PersonnelServices;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.user.enums.UserTokenType;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
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

import static cn.hamm.airpower.exception.Errors.*;
import static cn.hamm.spms.common.exception.CustomError.EMAIL_SEND_BUSY;
import static cn.hamm.spms.common.exception.CustomError.USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID;

/**
 * <h1>用户</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class UserService extends BaseService<UserEntity, UserRepository> {
    /**
     * 密码盐的长度
     */
    public static final int PASSWORD_SALT_LENGTH = 4;

    /**
     * 邮箱验证码连续错误次数上限
     */
    public static final int EMAIL_MAX_ERROR_COUNT = 5;

    /**
     * 全局每分钟最多发信次数
     */
    private static final int EMAIL_MAX_SEND_PER_IP = 20;

    /**
     * 取不到客户端 IP 时的占位值
     */
    private static final String UNKNOWN_IP = "unknown";

    /**
     * 验证码缓存时长（秒）
     */
    private static final int CACHE_CODE_EXPIRE_SECOND = DateTimeUtil.SECOND_PER_MINUTE * 5;

    /**
     * 用户所在房间的缓存 Key 前缀
     */
    private final String CACHE_ROOM_KEY = "room:user:";

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private EmailHelper emailHelper;

    @Autowired
    private CookieHelper cookieHelper;

    @Autowired
    private AccessConfig accessConfig;

    /**
     * 生成邮箱验证码
     *
     * @return 6 位数字验证码
     */
    private static @NotNull String getRandomValidateCode() {
        return RandomUtil.randomNumbers(6);
    }

    /**
     * 获取邮箱验证码的缓存 Key
     *
     * @param email 邮箱
     * @return 缓存 Key
     */
    @Contract(pure = true)
    private static @NotNull String getEmailCodeCacheKey(String email) {
        return "email:" + email + ":code:";
    }

    /**
     * 获取 Cookie 与用户的映射缓存 Key
     *
     * @param cookie Cookie 值
     * @return 缓存 Key
     */
    @Contract(pure = true)
    private static @NotNull String getCookieUserKey(String cookie) {
        return "cookie:" + cookie + ":user";
    }

    /**
     * 重置用户密码
     *
     * @param user        待修改密码的用户
     * @param newPassword 新密码
     * @apiNote 每次重置都会重新生成盐
     */
    private void resetPassword(@NotNull UserEntity user, String newPassword) {
        String salt = RandomUtil.randomString(PASSWORD_SALT_LENGTH);
        user.setSalt(salt);
        user.setPassword(PermissionUtil.encodePassword(newPassword, salt));
        updateToDatabase(user);
    }

    /**
     * 获取登录用户的菜单列表
     *
     * @param userId 用户 ID
     * @return 菜单树列表
     * @apiNote 超管（{@code id == 1}）直接返回全部菜单，不经过角色
     */
    public List<MenuEntity> getMenuListByUserId(long userId) {
        UserEntity user = get(userId);
        if (user.isRootUser()) {
            return TreeUtil.buildTreeList(
                    SystemServices.getMenuService().filter(new MenuEntity(), new Sort().setField("orderNo"))
            );
        }
        List<MenuEntity> menuList = new ArrayList<>();
        user.getRoleList().forEach(role -> role.getMenuList()
                .forEach(menu -> {
                    boolean isExist = menuList.stream()
                            .anyMatch(existMenu -> Objects.equals(menu.getId(), existMenu.getId()));
                    if (!isExist) {
                        menuList.add(menu);
                    }
                })
        );
        return TreeUtil.buildTreeList(menuList.stream().peek(item -> {
            item.excludeNotMeta();
            item.setIsPublished(null);
        }).toList());
    }

    /**
     * 获取登录用户的权限列表
     *
     * @param userId 用户 ID
     * @return 权限列表
     * @apiNote 超管（{@code id == 1}）直接返回全部权限，不经过角色
     */
    public List<PermissionEntity> getPermissionListByUserId(long userId) {
        UserEntity user = get(userId);
        if (user.isRootUser()) {
            return SystemServices.getPermissionService().getList(null);
        }
        List<PermissionEntity> permissionList = new ArrayList<>();
        user.getRoleList().forEach(roleEntity -> roleEntity.getPermissionList()
                .forEach(permission -> {
                    boolean isExist = permissionList.stream()
                            .anyMatch(existPermission -> Objects.equals(permission.getId(), existPermission.getId()));
                    if (!isExist) {
                        permissionList.add(permission);
                    }
                })
        );
        return permissionList;
    }

    /**
     * 修改密码
     *
     * @param userId      用户 ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public void modifyPassword(long userId, String oldPassword, String newPassword) {
        UserEntity existUser = get(userId);

        PARAM_INVALID.whenNotEqualsIgnoreCase(
                PermissionUtil.encodePassword(oldPassword, existUser.getSalt()),
                existUser.getPassword(),
                "原密码输入错误，修改密码失败"
        );
        String salt = RandomUtil.randomString();
        existUser.setSalt(salt);
        existUser.setPassword(PermissionUtil.encodePassword(newPassword, salt));
        updateToDatabase(existUser);
    }

    /**
     * 通过邮箱验证码重置密码
     *
     * @param email       邮箱
     * @param code        验证码
     * @param newPassword 新密码
     * @param clientIp    客户端 IP
     * @apiNote 成功后立即失效验证码，防止同一验证码被重复使用
     */
    public void resetPasswordViaEmail(String email, String code, String newPassword, @NotNull String clientIp) {
        validEmailAndCode(email, code, clientIp);
        UserEntity user = repository.getByEmail(email);
        PARAM_INVALID.whenNull(user, "重置密码失败，用户信息异常");
        resetPassword(user, newPassword);
        deleteEmailCode(email);
    }

    /**
     * 发送邮箱验证码
     *
     * @param email    邮箱
     * @param clientIp 客户端 IP
     * @apiNote 限流分目标邮箱、客户端 IP、全局三个维度，缺一不可：
     * 分别防单账号被刷、循环不同邮箱把服务器当邮件中继、大量 IP 各自发起
     */
    public void sendEmailCode(String email, @NotNull String clientIp) throws MessagingException {
        EMAIL_SEND_BUSY.when(redisHelper.hasKey(getEmailCodeCacheKey(email)));
        String ipKey = getEmailIpSendKey(clientIp);
        EMAIL_SEND_BUSY.when(redisHelper.hasKey(ipKey), "发送过于频繁，请两分钟后再试");
        String globalKey = getEmailGlobalSendKey();
        int sentThisMinute = parseIntQuietly(redisHelper.get(globalKey));
        EMAIL_SEND_BUSY.when(sentThisMinute >= EMAIL_MAX_SEND_PER_IP, "服务器邮件发送繁忙，请稍后再试");

        String code = getRandomValidateCode();
        redisHelper.set(getEmailCodeCacheKey(email), code, CACHE_CODE_EXPIRE_SECOND);
        redisHelper.set(ipKey, 1, DateTimeUtil.SECOND_PER_MINUTE * 2);
        redisHelper.set(globalKey, sentThisMinute + 1, DateTimeUtil.SECOND_PER_MINUTE);
        emailHelper.sendCode(email, "你收到一个邮箱验证码", code, appConfig.getProjectName());
    }

    /**
     * 读取并安全解析缓存中的计数
     *
     * @param value 缓存值
     * @return 整数值，解析失败按 0 处理
     * @apiNote 缓存被外部改写时不能因类型异常让整个登录/发信流程失败
     */
    private int parseIntQuietly(Object value) {
        if (Objects.isNull(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 登录并设置 Cookie
     *
     * @param response 响应
     * @param user     用户
     * @return AccessToken
     */
    public final String loginWithCookieAndResponse(@NotNull HttpServletResponse response, @NotNull UserEntity user) {
        String accessToken = createAccessToken(user.getId());

        String cookieString = RandomUtil.randomString();
        saveCookie(user.getId(), cookieString);
        Cookie cookie = cookieHelper.getAuthorizeCookie(cookieString);
        // 前端要读这个 Cookie（非 HttpOnly），是全站唯一例外
        cookie.setHttpOnly(false);
        cookie.setPath(CookieHelper.DEFAULT_PATH);
        response.addCookie(cookie);
        return accessToken;
    }

    /**
     * 存储 Cookie 与用户的映射
     *
     * @param userId 用户 ID
     * @param cookie Cookie 值
     */
    public void saveCookie(Long userId, String cookie) {
        redisHelper.set(getCookieUserKey(cookie), userId, DateTimeUtil.SECOND_PER_DAY);
    }

    /**
     * 通过 Cookie 反查用户
     *
     * @param cookie Cookie 值
     * @return 用户 ID，Cookie 不存在时返回 {@code null}
     */
    public Long getUserIdByCookie(String cookie) {
        Object userId = redisHelper.get(getCookieUserKey(cookie));
        if (Objects.isNull(userId)) {
            return null;
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * 邮箱与密码登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return 登录成功的用户
     * @apiNote 密码错误也会累加验证码失败计数；登录成功会清空计数并作废该邮箱的验证码
     */
    public UserEntity loginViaEmailAndPassword(String email, String password) {
        PARAM_INVALID.whenEmpty(email, "请确认传入有效的邮箱");
        PARAM_INVALID.whenEmpty(password, "请确认传入有效的密码");
        UserEntity existUser = repository.getByEmail(email);
        USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNull(existUser, "邮箱或密码错误");
        String encodePassword = PermissionUtil.encodePassword(password, existUser.getSalt());
        if (!encodePassword.equals(existUser.getPassword())) {
            addEmailFailCount(email, UNKNOWN_IP);
            throw new ServiceException(USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID, "邮箱或密码错误");
        }
        resetEmailFailCount(email);
        deleteEmailCode(email);
        return existUser;
    }

    /**
     * 累加邮箱验证码失败次数，达阈值时抛异常
     *
     * @param email    邮箱
     * @param clientIp 客户端 IP
     * @apiNote 计数键包含客户端 IP：同一 IP 输错只锁这个 IP，不会让邮箱的真正持有者无法登录。
     * 达阈值时<b>不删除验证码</b>，否则等于给攻击者一个远程锁死他人账号的手段
     */
    private void addEmailFailCount(String email, @NotNull String clientIp) {
        String key = getEmailFailKey(email, clientIp);
        int count = parseIntQuietly(redisHelper.get(key)) + 1;
        redisHelper.set(key, count, DateTimeUtil.SECOND_PER_HOUR);
        if (count >= EMAIL_MAX_ERROR_COUNT) {
            String ipKey = getEmailIpFailKey(clientIp);
            int ipCount = parseIntQuietly(redisHelper.get(ipKey)) + 1;
            redisHelper.set(ipKey, ipCount, DateTimeUtil.SECOND_PER_HOUR);
            throw new ServiceException("操作过于频繁，请一小时后重试");
        }
    }

    /**
     * 作废邮箱验证码
     *
     * @param email 邮箱
     */
    private void deleteEmailCode(String email) {
        redisHelper.delete(getEmailCodeCacheKey(email));
    }

    /**
     * 邮箱验证码登录
     *
     * @param email    邮箱
     * @param code     验证码
     * @param clientIp 客户端 IP
     * @return 登录成功的用户
     * @apiNote 邮箱尚未注册时，若 {@link ConfigFlag#AUTO_REGISTER_EMAIL_LOGIN} 开启会自动注册
     */
    public UserEntity loginViaEmailAndCode(String email, String code, @NotNull String clientIp) {
        validEmailAndCode(email, code, clientIp);
        UserEntity existUser = repository.getByEmail(email);
        if (Objects.nonNull(existUser)) {
            resetEmailFailCount(email);
            return existUser;
        }
        ConfigEntity configuration = SystemServices.getConfigService().get(ConfigFlag.AUTO_REGISTER_EMAIL_LOGIN);
        if (configuration.booleanConfig()) {
            existUser = registerUserViaEmail(email);
        }
        PARAM_INVALID.whenNull(existUser, "登录的邮箱账户不存在");
        resetEmailFailCount(email);
        return existUser;
    }

    /**
     * 验证邮箱和验证码，不匹配时累加失败次数
     *
     * @param email    邮箱
     * @param code     验证码
     * @param clientIp 客户端 IP
     */
    private void validEmailAndCode(String email, String code, @NotNull String clientIp) {
        PARAM_INVALID.whenEmpty(email, "请确认传入有效的邮箱");
        PARAM_INVALID.whenEmpty(code, "请确认传入有效的验证码");
        String cacheCode = getEmailCacheCode(email);
        if (!code.equalsIgnoreCase(cacheCode)) {
            addEmailFailCount(email, clientIp);
            throw new ServiceException(PARAM_INVALID, "邮箱验证码不正确");
        }
    }

    /**
     * 获取邮箱失败次数的缓存 Key
     *
     * @param email 邮箱
     * @return 缓存 Key
     */
    @Contract(pure = true)
    private @NotNull String getEmailFailKey(String email) {
        return getEmailFailKey(email, UNKNOWN_IP);
    }

    /**
     * 获取指定 IP 对指定邮箱的失败次数缓存 Key
     *
     * @param email    邮箱
     * @param clientIp 客户端 IP
     * @return 缓存 Key
     */
    private @NotNull String getEmailFailKey(String email, @NotNull String clientIp) {
        return "email:" + email + ":fail:" + clientIp;
    }

    /**
     * 获取指定 IP 的失败总次数缓存 Key
     *
     * @param clientIp 客户端 IP
     * @return 缓存 Key
     * @apiNote 用于封禁该 IP 换邮箱继续尝试
     */
    private @NotNull String getEmailIpFailKey(@NotNull String clientIp) {
        return "email:ip:" + clientIp + ":fail";
    }

    /**
     * 获取指定 IP 的发信频率缓存 Key
     *
     * @param clientIp 客户端 IP
     * @return 缓存 Key
     */
    private @NotNull String getEmailIpSendKey(@NotNull String clientIp) {
        return "email:ip:" + clientIp + ":send";
    }

    /**
     * 获取全局发信频率缓存 Key
     *
     * @return 缓存 Key
     */
    @Contract(pure = true)
    private @NotNull String getEmailGlobalSendKey() {
        return "email:global:send";
    }

    /**
     * 清零邮箱失败次数
     *
     * @param email 邮箱
     */
    private void resetEmailFailCount(String email) {
        redisHelper.delete(getEmailFailKey(email));
    }

    /**
     * 用邮箱和随机密码注册
     *
     * @param email 邮箱
     * @return 注册的用户
     */
    public UserEntity registerUserViaEmail(@NotNull String email) {
        return registerUserViaEmail(email, RandomUtil.randomString());
    }

    /**
     * 用邮箱和指定密码注册
     *
     * @param email    邮箱
     * @param password 密码
     * @return 注册的用户
     */
    public UserEntity registerUserViaEmail(@NotNull String email, String password) {
        String nickname = email.split("@")[0];
        String salt = RandomUtil.randomString(PASSWORD_SALT_LENGTH);
        UserEntity user = new UserEntity()
                .setEmail(email)
                .setPassword(PermissionUtil.encodePassword(password, salt))
                .setSalt(salt)
                .setNickname(nickname);
        long id = add(user);
        return get(id);
    }

    /**
     * 创建 AccessToken
     *
     * @param userId 用户 ID
     * @return AccessToken
     */
    public String createAccessToken(long userId) {
        return AccessTokenUtil.create().setPayloadId(userId)
                .setExpireSecond(accessConfig.getAuthorizeExpireSecond())
                .addPayload(UserTokenType.TYPE, UserTokenType.NORMAL.getKey())
                .build(apiConfig.getAccessTokenSecret());
    }

    /**
     * 获取指定邮箱缓存的验证码
     *
     * @param email 邮箱
     * @return 验证码
     */
    private String getEmailCacheCode(String email) {
        Object code = redisHelper.get(getEmailCodeCacheKey(email));
        return Objects.isNull(code) ? "" : code.toString();
    }

    @Override
    protected void beforeAppDelete(@NotNull UserEntity user) {
        FORBIDDEN_DELETE.when(user.isRootUser(), "系统内置用户无法被删除!");
    }

    @Override
    protected @NotNull UserEntity beforeAppAdd(@NotNull UserEntity user) {
        UserEntity existUser = repository.getByEmail(user.getEmail());
        FORBIDDEN_EXIST.whenNotNull(existUser, "邮箱已经存在，请勿重复添加用户");
        if (!StringUtils.hasLength(user.getPassword())) {
            String salt = RandomUtil.randomString(PASSWORD_SALT_LENGTH);
            user.setPassword(PermissionUtil.encodePassword(RandomUtil.randomString(), salt));
            user.setSalt(salt);
        }
        return user;
    }

    /**
     * 读取用户时组装角色
     *
     * @param user 用户
     * @return 组装后的用户
     * @apiNote {@code roleList} 是 {@code @Transient}（关联由 {@code user_role_link} 中间表承载），
     * 必须显式组装；且要级联组装角色的菜单与权限，因为 open-in-view 已关闭，
     * 鉴权链路在事务外会一路读到 {@code role.getPermissionList()}
     */
    @Override
    protected @NotNull UserEntity afterAppGet(@NotNull UserEntity user) {
        fillRoleList(user);
        return user;
    }

    @Override
    protected @NotNull List<UserEntity> afterGetList(@NotNull List<UserEntity> list) {
        list.forEach(this::fillRoleList);
        return list;
    }

    /**
     * 组装单个用户的角色及其菜单与权限
     *
     * @param user 用户
     */
    private void fillRoleList(@NotNull UserEntity user) {
        if (Objects.isNull(user.getId())) {
            return;
        }
        Set<RoleEntity> roles = PersonnelServices.getUserRoleLinkService().getRoles(user.getId());
        PersonnelServices.getRoleService().fillLinksForRoles(roles);
        user.setRoleList(new LinkedHashSet<>(roles));
    }

    @Override
    protected void afterAppAdd(long id, @NotNull UserEntity source) {
        if (Objects.isNull(source.getRoleList())) {
            return;
        }
        PersonnelServices.getUserRoleLinkService().syncByUserId(id, source.getRoleList());
    }

    /**
     * 修改后同步角色
     *
     * @param id     用户 ID
     * @param source 客户端提交的用户
     * @apiNote 必须判 {@code null}：前端编辑用户基本信息时通常不传 roleList，
     * Jackson 反序列化后是 {@code null}。不区分「未传」与「传空集」的话，
     * 一次普通的信息修改就会把用户角色全部清空
     */
    @Override
    protected void afterAppUpdate(long id, @NotNull UserEntity source) {
        if (Objects.isNull(source.getRoleList())) {
            return;
        }
        PersonnelServices.getUserRoleLinkService().syncByUserId(id, source.getRoleList());
    }

    @Override
    protected void beforeAppDisable(@NotNull UserEntity existUser) {
        FORBIDDEN_DISABLED_NOT_ALLOWED.when(existUser.isRootUser(), "系统内置用户无法被禁用!");
    }

    @Override
    protected @NotNull List<Predicate> addSearchPredicate(@NotNull Root<UserEntity> root, @NotNull CriteriaBuilder builder, @NotNull UserEntity search) {
        Long departmentId = search.getDepartmentId();
        if (Objects.isNull(departmentId)) {
            return new ArrayList<>();
        }
        List<Predicate> predicateList = new ArrayList<>();
        Set<Long> departmentIdList = PersonnelServices.getDepartmentService().getListByParentId(departmentId);
        departmentIdList.add(departmentId);
        Join<UserEntity, DepartmentEntity> departmentJoin = root.join("departmentList");
        Predicate inPredicate = departmentJoin.get(CurdEntity.STRING_ID).in(departmentIdList);
        predicateList.add(inPredicate);
        return predicateList;
    }

    /**
     * 获取当前用户所在的房间 ID
     *
     * @param userId 用户 ID
     * @return 房间 ID，无缓存时返回默认房间 ID
     */
    public long getCurrentRoomId(long userId) {
        Object data = redisHelper.get(CACHE_ROOM_KEY + userId);
        if (Objects.isNull(data)) {
            return appConfig.getDefaultRoomId();
        }
        return Integer.parseInt(data.toString());
    }

    /**
     * 保存当前用户所在的房间 ID
     *
     * @param userId 用户 ID
     * @param roomId 房间 ID
     */
    public void saveCurrentRoomId(long userId, long roomId) {
        redisHelper.set(CACHE_ROOM_KEY + userId, roomId, DateTimeUtil.SECOND_PER_DAY * 30);
    }

    /**
     * 判断用户当前是否确实在指定房间
     *
     * @param userId 用户 ID
     * @param roomId 房间 ID
     * @return 缓存存在且指向该房间才返回 {@code true}
     * @apiNote 判断「是否在房间里」必须用本方法。{@link #getCurrentRoomId(long)} 在无缓存时
     * 返回默认房间 ID，并不代表用户真的在那里
     */
    public boolean isInRoom(long userId, long roomId) {
        if (roomId <= 0) {
            return false;
        }
        Object data = redisHelper.get(CACHE_ROOM_KEY + userId);
        if (Objects.isNull(data)) {
            return false;
        }
        return Integer.parseInt(data.toString()) == roomId;
    }

    /**
     * 清除用户当前所在房间的缓存
     *
     * @param userId 用户 ID
     */
    public void clearCurrentRoomId(long userId) {
        redisHelper.delete(CACHE_ROOM_KEY + userId);
    }
}
