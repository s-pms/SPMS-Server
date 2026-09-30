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
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.hamm.airpower.exception.Errors.*;
import static cn.hamm.spms.common.exception.CustomError.EMAIL_SEND_BUSY;
import static cn.hamm.spms.common.exception.CustomError.USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID;

/**
 * <h1>Service</h1>
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
     * 邮箱最大错误次数
     */
    public static final int EMAIL_MAX_ERROR_COUNT = 5;
    /**
     * 同一 IP 每分钟最多触发的发信次数（全局维度兜底）
     */
    private static final int EMAIL_MAX_SEND_PER_IP = 20;
    /**
     * 无法获取客户端 IP 时的占位值
     */
    private static final String UNKNOWN_IP = "unknown";

    /**
     * Code 缓存秒数
     */
    private static final int CACHE_CODE_EXPIRE_SECOND = DateTimeUtil.SECOND_PER_MINUTE * 5;

    /**
     * 缓存房间用户
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
     * 获取新的密码盐
     *
     * @return 密码盐
     */
    private static @NotNull String getRandomValidateCode() {
        return RandomUtil.randomNumbers(6);
    }

    /**
     * 获取邮箱验证码的缓存 key
     *
     * @param email 邮箱
     * @return 缓存 Key
     */
    @Contract(pure = true)
    private static @NotNull String getEmailCodeCacheKey(String email) {
        return "email:" + email + ":code:";
    }

    /**
     * 获取 Cookie 的缓存 key
     *
     * @param cookie Cookie
     * @return 缓存 Key
     */
    @Contract(pure = true)
    private static @NotNull String getCookieUserKey(String cookie) {
        return "cookie:" + cookie + ":user";
    }

    /**
     * 重置密码
     *
     * @param user        待修改密码的用户
     * @param newPassword 新密码
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
     * @param userId      用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public void modifyPassword(long userId, String oldPassword, String newPassword) {
        UserEntity existUser = get(userId);

        // 判断原始密码
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
     * 通过邮箱重置密码
     *
     * @param email       邮箱
     * @param code        验证码
     * @param newPassword 新密码
     */
    public void resetPasswordViaEmail(String email, String code, String newPassword) {
        resetPasswordViaEmail(email, code, newPassword, UNKNOWN_IP);
    }

    /**
     * 通过邮箱验证码重置密码
     *
     * @param email     邮箱
     * @param code      验证码
     * @param newPassword 新密码
     * @param clientIp  客户端 IP，用于失败次数限流
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
     * @param email 邮箱
     */
    public void sendEmailCode(String email) throws MessagingException {
        sendEmailCode(email, UNKNOWN_IP);
    }

    /**
     * 发送邮箱验证码
     * <p>
     * 限流分三个维度，缺一不可：
     * <ul>
     *     <li>目标邮箱：防止单账号被刷验证码</li>
     *     <li>客户端 IP：防止循环不同邮箱把服务器当邮件中继</li>
     *     <li>全局：兜底，防止大量 IP 各自发起</li>
     * </ul>
     * </p>
     *
     * @param email    邮箱
     * @param clientIp 客户端 IP
     */
    public void sendEmailCode(String email, @NotNull String clientIp) throws MessagingException {
        EMAIL_SEND_BUSY.when(redisHelper.hasKey(getEmailCodeCacheKey(email)));
        // IP 维度：同一 IP 两分钟内只能发一次，无论目标是哪个邮箱
        String ipKey = getEmailIpSendKey(clientIp);
        EMAIL_SEND_BUSY.when(redisHelper.hasKey(ipKey), "发送过于频繁，请两分钟后再试");
        // 全局维度：无论来源 IP，一分钟内最多放行 20 次
        String globalKey = getEmailGlobalSendKey();
        int sentThisMinute = Objects.requireNonNullElse(parseIntQuietly(redisHelper.get(globalKey)), 0);
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
     */
    private @Nullable Integer parseIntQuietly(@Nullable Object value) {
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
        // 创建 AccessToken
        String accessToken = createAccessToken(user.getId());

        // 存储 Cookies
        String cookieString = RandomUtil.randomString();
        saveCookie(user.getId(), cookieString);
        Cookie cookie = cookieHelper.getAuthorizeCookie(cookieString);
        cookie.setHttpOnly(false);
        cookie.setPath(CookieHelper.DEFAULT_PATH);
        response.addCookie(cookie);
        return accessToken;
    }

    /**
     * 存储 Cookie
     *
     * @param userId UserId
     * @param cookie Cookie
     */
    public void saveCookie(Long userId, String cookie) {
        redisHelper.set(getCookieUserKey(cookie), userId, DateTimeUtil.SECOND_PER_DAY);
    }

    /**
     * 通过 Cookie 获取一个用户
     *
     * @param cookie Cookie
     * @return UserId
     */
    public Long getUserIdByCookie(String cookie) {
        Object userId = redisHelper.get(getCookieUserKey(cookie));
        if (Objects.isNull(userId)) {
            return null;
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * 账号密码登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return 登录成功的用户
     */
    public UserEntity loginViaEmailAndPassword(String email, String password) {
        PARAM_INVALID.whenEmpty(email, "请确认传入有效的邮箱");
        PARAM_INVALID.whenEmpty(password, "请确认传入有效的密码");
        UserEntity existUser = repository.getByEmail(email);
        USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNull(existUser, "邮箱或密码错误");
        // 将用户传入的密码加密与数据库存储匹配
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
     * 累加邮箱验证码失败次数
     * <p>
     * 计数键包含客户端 IP：同一 IP 对同一邮箱连续输错只锁定这个 IP 的后续尝试，
     * 不会因为一个人的错误操作而让该邮箱的真正持有者无法登录。
     * <b>达阈值时也不再删除验证码</b> —— 旧实现会顺手删掉受害者已经收到的验证码，
     * 等于给了攻击者一个远程锁死他人账号的手段。
     * </p>
     *
     * @param email    邮箱
     * @param clientIp 客户端 IP
     */
    private void addEmailFailCount(String email, @NotNull String clientIp) {
        String key = getEmailFailKey(email, clientIp);
        int count = Objects.requireNonNullElse(parseIntQuietly(redisHelper.get(key)), 0) + 1;
        redisHelper.set(key, count, DateTimeUtil.SECOND_PER_HOUR);
        if (count >= EMAIL_MAX_ERROR_COUNT) {
            // 顺带封禁该 IP 对所有邮箱的尝试，挡住换邮箱继续试
            String ipKey = getEmailIpFailKey(clientIp);
            int ipCount = Objects.requireNonNullElse(parseIntQuietly(redisHelper.get(ipKey)), 0) + 1;
            redisHelper.set(ipKey, ipCount, DateTimeUtil.SECOND_PER_HOUR);
            throw new ServiceException("操作过于频繁，请一小时后重试");
        }
    }

    private void deleteEmailCode(String email) {
        redisHelper.delete(getEmailCodeCacheKey(email));
    }

    /**
     * 邮箱验证码登录
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 登录成功的用户
     */
    public UserEntity loginViaEmailAndCode(String email, String code) {
        return loginViaEmailAndCode(email, code, UNKNOWN_IP);
    }

    /**
     * 邮箱验证码登录
     *
     * @param email    邮箱
     * @param code     验证码
     * @param clientIp 客户端 IP，用于失败次数限流
     * @return 登录成功的用户
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
            // 注册一个用户
            existUser = registerUserViaEmail(email);
        }
        PARAM_INVALID.whenNull(existUser, "登录的邮箱账户不存在");
        resetEmailFailCount(email);
        return existUser;
    }

    /**
     * 验证邮箱和验证码
     *
     * @param email 邮箱
     * @param code  验证码
     */
    private void validEmailAndCode(String email, String code) {
        validEmailAndCode(email, code, UNKNOWN_IP);
    }

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
     * 获取指定 IP 的失败总次数缓存 Key（用于封禁换邮箱继续尝试）
     *
     * @param clientIp 客户端 IP
     * @return 缓存 Key
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
     * 重置邮箱失败次数
     *
     * @param email 邮箱
     */
    private void resetEmailFailCount(String email) {
        redisHelper.delete(getEmailFailKey(email));
    }

    /**
     * 邮箱和随机密码注册
     *
     * @param email 邮箱
     * @return 注册的用户
     */
    public UserEntity registerUserViaEmail(@NotNull String email) {
        return registerUserViaEmail(email, RandomUtil.randomString());
    }

    /**
     * 邮箱和指定密码注册
     *
     * @param email    邮箱
     * @param password 密码
     * @return 注册的用户
     */
    public UserEntity registerUserViaEmail(@NotNull String email, String password) {
        // 昵称默认为邮箱账号 @ 前面的
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
    protected void beforeDelete(@NotNull UserEntity user) {
        FORBIDDEN_DELETE.when(user.isRootUser(), "系统内置用户无法被删除!");
    }

    @Override
    protected @NotNull UserEntity beforeAdd(@NotNull UserEntity user) {
        UserEntity existUser = repository.getByEmail(user.getEmail());
        FORBIDDEN_EXIST.whenNotNull(existUser, "邮箱已经存在，请勿重复添加用户");
        if (!StringUtils.hasLength(user.getPassword())) {
            // 创建时没有设置密码的话 随机一个密码
            String salt = RandomUtil.randomString(PASSWORD_SALT_LENGTH);
            user.setPassword(PermissionUtil.encodePassword(RandomUtil.randomString(), salt));
            user.setSalt(salt);
        }
        return user;
    }

    @Override
    protected void beforeDisable(@NotNull UserEntity existUser) {
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
     * @return 房间 ID
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
     * <p>
     * 与 {@link #getCurrentRoomId(long)} 的区别：后者在 Redis 无缓存时会返回
     * 默认房间 ID（{@code app.chat.defaultRoomId}，一般为 1），
     * 并不代表用户真的在那里。判断「是否在房间里」必须用本方法。
     * </p>
     *
     * @param userId 用户 ID
     * @param roomId 房间 ID
     * @return 缓存存在且指向该房间才返回 true
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
