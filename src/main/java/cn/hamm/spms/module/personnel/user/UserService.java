package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.airpower.model.Sort;
import cn.hamm.airpower.util.EmailUtil;
import cn.hamm.airpower.util.PasswordUtil;
import cn.hamm.airpower.util.TreeUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.config.AppConstant;
import cn.hamm.spms.common.exception.CustomError;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class UserService extends BaseService<UserEntity, UserRepository> {
    /**
     * <h2>邮箱验证码key</h2>
     */
    private static final String REDIS_EMAIL_CODE_KEY = "email_code_";
    /**
     * <h2>短信验证码key</h2>
     */
    private static final String REDIS_PHONE_CODE_KEY = "phone_code_";
    /**
     * <h2>OAUTH存储的key前缀</h2>
     */
    private static final String OAUTH_CODE_KEY = "oauth_code_";
    /**
     * <h2>COOKIE前缀</h2>
     */
    private static final String COOKIE_CODE_KEY = "cookie_code_";

    /**
     * <h2>Code缓存 包含了 Oauth2的 Code 和 验证码的 Code</h2>
     */
    private static final int CACHE_CODE_EXPIRE_SECOND = Constant.SECOND_PER_MINUTE * 5;
    /**
     * <h2>Cookie缓存</h2>
     */
    private static final int CACHE_COOKIE_EXPIRE_SECOND = Constant.SECOND_PER_DAY;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private TreeUtil treeUtil;

    @Autowired
    private EmailUtil emailUtil;

    /**
     * <h2>获取登录用户的菜单列表</h2>
     *
     * @param userId 用户id
     * @return 菜单树列表
     */
    public List<MenuEntity> getMenuListByUserId(long userId) {
        UserEntity user = get(userId);
        if (user.isRootUser()) {
            MenuService menuService = Services.getMenuService();
            return treeUtil.buildTreeList(
                    menuService.filter(null, new Sort().setField(AppConstant.ORDER_NO))
            );
        }
        List<MenuEntity> menuList = new ArrayList<>();
        user.getRoleList().forEach(role -> role.getMenuList().forEach(menu -> {
            boolean isExist = menuList.stream()
                    .anyMatch(existMenu -> menu.getId().equals(existMenu.getId()));
            if (!isExist) {
                menuList.add(menu);
            }
        }));
        return treeUtil.buildTreeList(menuList);
    }

    /**
     * <h2>获取登录用户的权限列表</h2>
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
        user.getRoleList().forEach(role -> role.getPermissionList().forEach(permission -> {
            boolean isExist = permissionList.stream()
                    .anyMatch(existPermission -> permission.getId().equals(existPermission.getId()));
            if (!isExist) {
                permissionList.add(permission);
            }
        }));
        return permissionList;
    }

    /**
     * <h2>修改密码</h2>
     *
     * @param user 用户信息
     */
    public void modifyUserPassword(@NotNull UserEntity user) {
        UserEntity existUser = get(user.getId());
        String code = getEmailCode(existUser.getEmail());
        ServiceError.PARAM_INVALID.whenNotEquals(code, user.getCode(), "验证码输入错误");
        String oldPassword = user.getOldPassword();
        ServiceError.PARAM_INVALID.whenNotEqualsIgnoreCase(
                passwordUtil.encode(oldPassword, existUser.getSalt()),
                existUser.getPassword(),
                "原密码输入错误，修改密码失败"
        );
        String salt = randomUtil.randomString(AppConstant.PASSWORD_SALT_LENGTH);
        user.setSalt(salt);
        user.setPassword(passwordUtil.encode(user.getPassword(), salt));
        removeEmailCodeCache(existUser.getEmail());
        update(user);
    }

    /**
     * <h2>删除指定邮箱的验证码缓存</h2>
     *
     * @param email 邮箱
     */
    public void removeEmailCodeCache(String email) {
        redisUtil.del(REDIS_EMAIL_CODE_KEY + email);
    }

    /**
     * <h2>发送邮箱验证码</h2>
     *
     * @param email 邮箱
     */
    public void sendMail(String email) throws MessagingException {
        CustomError.EMAIL_SEND_BUSY.when(hasEmailCodeInRedis(email));
        String code = randomUtil.randomNumbers(6);
        setCodeToRedis(email, code);
        emailUtil.sendCode(email, "你收到一个邮箱验证码", code, "SPMS");
    }

    /**
     * <h2>存储Oauth的一次性Code</h2>
     *
     * @param userId  用户ID
     * @param openApp 保存的应用信息
     */
    public void saveOauthCode(Long userId, @NotNull OpenAppEntity openApp) {
        redisUtil.set(getAppCodeKey(openApp.getAppKey(), openApp.getCode()), userId, CACHE_CODE_EXPIRE_SECOND);
    }

    /**
     * <h2>获取指定应用的OauthCode缓存Key</h2>
     *
     * @param appKey 应用Key
     * @param code   Code
     * @return 缓存的Key
     */
    protected String getAppCodeKey(String appKey, String code) {
        return OAUTH_CODE_KEY + appKey + Constant.UNDERLINE + code;
    }

    /**
     * <h2>通过AppKey和Code获取用户ID</h2>
     *
     * @param appKey AppKey
     * @param code   Code
     * @return UserId
     */
    public Long getUserIdByOauthAppKeyAndCode(String appKey, String code) {
        Object userId = redisUtil.get(getAppCodeKey(appKey, code));
        ServiceError.FORBIDDEN.whenNull(userId, "你的AppKey或Code错误，请重新获取");
        return Long.valueOf(userId.toString());
    }

    /**
     * <h2>删除AppOauthCode缓存</h2>
     *
     * @param appKey AppKey
     * @param code   Code
     */
    public void removeOauthCode(String appKey, String code) {
        redisUtil.del(getAppCodeKey(appKey, code));
    }

    /**
     * <h2>存储Cookie</h2>
     *
     * @param userId UserId
     * @param cookie Cookie
     */
    public void saveCookie(Long userId, String cookie) {
        redisUtil.set(COOKIE_CODE_KEY + cookie, userId, CACHE_COOKIE_EXPIRE_SECOND);
    }

    /**
     * <h2>通过Cookie获取一个用户</h2>
     *
     * @param cookie Cookie
     * @return UserId
     */
    public Long getUserIdByCookie(String cookie) {
        Object userId = redisUtil.get(COOKIE_CODE_KEY + cookie);
        if (Objects.isNull(userId)) {
            return null;
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * <h2>ID+密码 账号+密码</h2>
     *
     * @param userEntity 用户实体
     * @return AccessToken
     */
    public String login(@NotNull UserEntity userEntity) {
        UserEntity existUser = null;
        if (Objects.nonNull(userEntity.getId())) {
            // ID登录
            existUser = getMaybeNull(userEntity.getId());
        } else if (Objects.nonNull(userEntity.getAccount())) {
            // 账号登录
            existUser = repository.getByAccount(userEntity.getAccount());
        } else {
            ServiceError.PARAM_INVALID.show("ID或账号不能为空，请确认是否传入");
        }
        CustomError.USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNull(existUser);
        // 将用户传入的密码加密与数据库存储匹配
        String encodePassword = passwordUtil.encode(userEntity.getPassword(), existUser.getSalt());
        CustomError.USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNotEqualsIgnoreCase(encodePassword, existUser.getPassword());
        return securityUtil.createAccessToken(existUser.getId());
    }

    /**
     * <h2>邮箱验证码登录</h2>
     *
     * @param userEntity 用户实体
     * @return AccessToken
     */
    public String loginViaEmail(@NotNull UserEntity userEntity) {
        String code = getEmailCode(userEntity.getEmail());
        ServiceError.PARAM_INVALID.whenNotEquals(code, userEntity.getCode(), "邮箱验证码不正确");
        UserEntity existUser = repository.getByEmail(userEntity.getEmail());
        ServiceError.PARAM_INVALID.whenNull("邮箱或验证码不正确");
        return securityUtil.createAccessToken(existUser.getId());
    }

    /**
     * <h2>短信验证码登录</h2>
     *
     * @param userEntity 用户实体
     * @return AccessToken
     */
    public String loginViaPhone(@NotNull UserEntity userEntity) {
        String code = getPhoneCode(userEntity.getEmail());
        ServiceError.PARAM_INVALID.whenNotEquals(code, userEntity.getCode(), "短信验证码不正确");
        UserEntity existUser = repository.getByPhone(userEntity.getEmail());
        ServiceError.PARAM_INVALID.whenNull("手机或验证码不正确");
        return securityUtil.createAccessToken(existUser.getId());
    }

    /**
     * <h2>将验证码暂存到Redis</h2>
     *
     * @param email 邮箱
     * @param code  验证码
     */
    private void setCodeToRedis(String email, String code) {
        redisUtil.set(REDIS_EMAIL_CODE_KEY + email, code, CACHE_CODE_EXPIRE_SECOND);
    }

    /**
     * <h2>获取指定邮箱发送的验证码</h2>
     *
     * @param email 邮箱
     * @return 验证码
     */
    private String getEmailCode(String email) {
        Object code = redisUtil.get(REDIS_EMAIL_CODE_KEY + email);
        return Objects.isNull(code) ? Constant.EMPTY_STRING : code.toString();
    }

    /**
     * <h2>获取指定邮箱发送的验证码</h2>
     *
     * @param email 邮箱
     * @return 验证码
     */
    private String getPhoneCode(String email) {
        Object code = redisUtil.get(REDIS_PHONE_CODE_KEY + email);
        return Objects.isNull(code) ? Constant.EMPTY_STRING : code.toString();
    }


    /**
     * <h2>指定邮箱验证码是否还在缓存内</h2>
     *
     * @param email 邮箱
     * @return 是否在缓存内
     */
    private boolean hasEmailCodeInRedis(String email) {
        return redisUtil.hasKey(REDIS_EMAIL_CODE_KEY + email);
    }

    @Override
    protected void beforeDelete(long id) {
        UserEntity entity = get(id);
        ServiceError.FORBIDDEN_DELETE.when(entity.isRootUser(), "该超级管理员用户无法被删除!");
    }

    @Override
    protected @NotNull UserEntity beforeAdd(@NotNull UserEntity source) {
        UserEntity existUser = repository.getByEmail(source.getEmail());
        ServiceError.FORBIDDEN_EXIST.whenNotNull(existUser, "邮箱已经存在，请勿重复添加用户");
        if (!StringUtils.hasLength(source.getPassword())) {
            // 创建时没有设置密码的话 随机一个密码
            String salt = randomUtil.randomString(AppConstant.PASSWORD_SALT_LENGTH);
            source.setPassword(passwordUtil.encode("123123", salt));
            source.setSalt(salt);
        }
        return source;
    }
}
