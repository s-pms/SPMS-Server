package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.model.Sort;
import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.security.PasswordUtil;
import cn.hamm.airpower.security.SecurityUtil;
import cn.hamm.airpower.util.EmailUtil;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.airpower.util.TreeUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.exception.CustomResult;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.system.app.AppEntity;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import cn.hamm.spms.module.system.permission.PermissionService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
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
     * <h3>Code缓存 包含了 Oauth2的 Code 和 验证码的 Code</h3>
     */
    private static final int CACHE_CODE_EXPIRE_SECOND = 300;

    /**
     * <h2>Cookie缓存</h2>
     */
    private static final int CACHE_COOKIE_EXPIRE_SECOND = 86400;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private MenuService menuService;

    @Autowired
    private PermissionService permissionService;

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
        UserEntity userEntity = get(userId);
        if (userEntity.isRootUser()) {
            return treeUtil.buildTreeList(menuService.getList(new QueryRequest<MenuEntity>().setSort(new Sort().setField("orderNo"))));
        }
        List<MenuEntity> menuList = new ArrayList<>();
        for (RoleEntity roleEntity : userEntity.getRoleList()) {
            roleEntity.getMenuList().forEach(menuItem -> {
                boolean isExist = false;
                for (MenuEntity existItem : menuList) {
                    if (menuItem.getId().equals(existItem.getId())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    menuList.add(menuItem);
                }
            });
        }
        return treeUtil.buildTreeList(menuList);
    }

    /**
     * <h2>获取登录用户的权限列表</h2>
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<PermissionEntity> getPermissionListByUserId(long userId) {
        UserEntity userEntity = get(userId);
        if (userEntity.isRootUser()) {
            return permissionService.getList(null);
        }
        List<PermissionEntity> permissionList = new ArrayList<>();
        for (RoleEntity roleEntity : userEntity.getRoleList()) {
            roleEntity.getPermissionList().forEach(permissionItem -> {
                boolean isExist = false;
                for (PermissionEntity existItem : permissionList) {
                    if (permissionItem.getId().equals(existItem.getId())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    permissionList.add(permissionItem);
                }
            });
        }
        return permissionList;
    }

    /**
     * <h2>修改密码</h2>
     *
     * @param user 用户信息
     */
    public void modifyUserPassword(UserEntity user) {
        UserEntity existUser = get(user.getId());
        String code = getEmailCode(existUser.getEmail());
        Result.PARAM_INVALID.whenNotEquals(code, user.getCode(), "验证码输入错误");
        String oldPassword = user.getOldPassword();
        Result.PARAM_INVALID.whenNotEqualsIgnoreCase(
                PasswordUtil.encode(oldPassword, existUser.getSalt()),
                existUser.getPassword(),
                "原密码输入错误，修改密码失败"
        );
        String salt = RandomUtil.randomString(4);
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encode(user.getPassword(), salt));
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
        CustomResult.EMAIL_SEND_BUSY.when(hasEmailCodeInRedis(email));
        String code = RandomUtil.randomNumbers(6);
        setCodeToRedis(email, code);
        emailUtil.sendCode(email, "你收到一个邮箱验证码", code, "SPMS");
    }

    /**
     * <h2>存储Oauth的一次性Code</h2>
     *
     * @param userId    用户ID
     * @param appEntity 保存的应用信息
     */
    public void saveOauthCode(Long userId, AppEntity appEntity) {
        redisUtil.set(getAppCodeKey(appEntity.getAppKey(), appEntity.getCode()), userId, CACHE_CODE_EXPIRE_SECOND);
    }

    /**
     * <h2>获取指定应用的OauthCode缓存Key</h2>
     *
     * @param appKey 应用Key
     * @param code   Code
     * @return 缓存的Key
     */
    protected String getAppCodeKey(String appKey, String code) {
        return OAUTH_CODE_KEY + appKey + "_" + code;
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
        Result.FORBIDDEN.whenNull(userId, "你的AppKey或Code错误，请重新获取");
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
    public String login(UserEntity userEntity) {
        UserEntity existUser = null;
        if (Objects.nonNull(userEntity.getId())) {
            // ID登录
            existUser = getMaybeNull(userEntity.getId());
        } else if (Objects.nonNull(userEntity.getAccount())) {
            // 账号登录
            existUser = repository.getByAccount(userEntity.getAccount());
        } else {
            Result.PARAM_INVALID.show("ID或账号不能为空，请确认是否传入");
        }
        CustomResult.USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNull(existUser);
        // 将用户传入的密码加密与数据库存储匹配
        assert existUser != null;
        String encodePassword = PasswordUtil.encode(userEntity.getPassword(), existUser.getSalt());
        CustomResult.USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID.whenNotEqualsIgnoreCase(encodePassword, existUser.getPassword());
        return securityUtil.createAccessToken(existUser.getId());
    }

    /**
     * <h2>邮箱验证码登录</h2>
     *
     * @param userEntity 用户实体
     * @return AccessToken
     */
    public String loginViaEmail(UserEntity userEntity) {
        String code = getEmailCode(userEntity.getEmail());
        Result.PARAM_INVALID.whenNotEquals(code, userEntity.getCode(), "邮箱验证码不正确");
        UserEntity existUser = repository.getByEmail(userEntity.getEmail());
        Result.PARAM_INVALID.whenNull("邮箱或验证码不正确");
        return securityUtil.createAccessToken(existUser.getId());
    }

    /**
     * <h2>短信验证码登录</h2>
     *
     * @param userEntity 用户实体
     * @return AccessToken
     */
    public String loginViaPhone(UserEntity userEntity) {
        String code = getPhoneCode(userEntity.getEmail());
        Result.PARAM_INVALID.whenNotEquals(code, userEntity.getCode(), "短信验证码不正确");
        UserEntity existUser = repository.getByPhone(userEntity.getEmail());
        Result.PARAM_INVALID.whenNull("手机或验证码不正确");
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
        return Objects.isNull(code) ? "" : code.toString();
    }

    /**
     * <h2>获取指定邮箱发送的验证码</h2>
     *
     * @param email 邮箱
     * @return 验证码
     */
    private String getPhoneCode(String email) {
        Object code = redisUtil.get(REDIS_PHONE_CODE_KEY + email);
        return Objects.isNull(code) ? "" : code.toString();
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
        Result.FORBIDDEN_DELETE.when(entity.isRootUser(), "该超级管理员用户无法被删除!");
    }

    @Override
    protected UserEntity beforeAdd(UserEntity source) {
        UserEntity existUser = repository.getByEmail(source.getEmail());
        Result.FORBIDDEN_EXIST.whenNotNull(existUser, "邮箱已经存在，请勿重复添加用户");
        if (!StringUtils.hasLength(source.getPassword())) {
            // 创建时没有设置密码的话 随机一个密码
            String salt = RandomUtil.randomString(4);
            source.setPassword(PasswordUtil.encode("123123", salt));
            source.setSalt(salt);
        }
        return source;
    }
}
