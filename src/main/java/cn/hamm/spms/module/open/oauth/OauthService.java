package cn.hamm.spms.module.open.oauth;

import cn.hamm.airpower.core.DateTimeUtil;
import cn.hamm.airpower.redis.RedisHelper;
import cn.hamm.spms.module.open.oauth.model.base.AbstractOauthCallback;
import cn.hamm.spms.module.open.oauth.model.base.OauthUserInfo;
import cn.hamm.spms.module.open.oauth.model.enums.OauthPlatform;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginEntity;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.enums.UserGender;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.DATA_NOT_FOUND;
import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;

/**
 * <h1>OauthService</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class OauthService {
    /**
     * Code 缓存秒数
     */
    private static final int CACHE_CODE_EXPIRE_SECOND = DateTimeUtil.SECOND_PER_MINUTE * 5;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private UserThirdLoginService userThirdLoginService;

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 用户 ID 的缓存 Key
     *
     * @param appKey 应用 Key
     * @param code   Code
     * @return 缓存的 Key
     */
    @Contract(pure = true)
    public static @NotNull String getUserIdCacheKey(String appKey, String code) {
        return "oauth_user_" + appKey + "_" + code;
    }

    /**
     * Scope 的缓存 Key
     *
     * @param appKey 应用 Key
     * @param code   Code
     * @return 缓存的 Key
     */
    @Contract(pure = true)
    public static @NotNull String getScopeCacheKey(String appKey, String code) {
        return "oauth_scope_" + appKey + "_" + code;
    }

    private static OauthPlatform getOauthPlatform(String platform) {
        OauthPlatform[] platforms = OauthPlatform.values();
        OauthPlatform oauthPlatform = Arrays.stream(platforms).filter(item -> item.getFlag().equals(platform)).findFirst().orElse(null);
        DATA_NOT_FOUND.whenNull(oauthPlatform, "暂不支持的第三方平台");
        return oauthPlatform;
    }

    private @NotNull AbstractOauthCallback getOauthCallbackInstance(@NotNull OauthPlatform oauthPlatform) {
        return beanFactory.getBean(oauthPlatform.getClazz());
    }

    /**
     * 缓存用户 ID
     *
     * @param appKey AppKey
     * @param code   Code
     * @param userId 用户 ID
     */
    public void saveOauthUserCache(String appKey, String code, long userId) {
        redisHelper.set(getUserIdCacheKey(appKey, code), userId, CACHE_CODE_EXPIRE_SECOND);
    }

    /**
     * 获取缓存的用户 ID
     *
     * @param appKey AppKey
     * @param code   Code
     * @return UserId
     */
    public Long getOauthUserCache(String appKey, String code) {
        Object userId = redisHelper.get(getUserIdCacheKey(appKey, code));
        FORBIDDEN.whenNull(userId, "你的 AppKey 或 Code 错误，请重新获取");
        return Long.valueOf(userId.toString());
    }

    /**
     * 删除缓存的用户 ID
     *
     * @param appKey AppKey
     * @param code   Code
     */
    public void removeOauthUserCache(String appKey, String code) {
        redisHelper.delete(getUserIdCacheKey(appKey, code));
    }

    /**
     * 删除缓存的 Scope
     *
     * @param appKey AppKey
     * @param code   Code
     */
    public void removeOauthScopeCache(String appKey, String code) {
        redisHelper.delete(getScopeCacheKey(appKey, code));
    }

    /**
     * 缓存 Scope
     *
     * @param appKey AppKey
     * @param code   Code
     * @param scope  Scope
     */
    public void saveOauthScopeCache(String appKey, String code, String scope) {
        redisHelper.set(getScopeCacheKey(appKey, code), scope, CACHE_CODE_EXPIRE_SECOND);
    }

    /**
     * 获取缓存的 Scope
     *
     * @param appKey AppKey
     * @param code   Code
     * @return Scope
     */
    public String getOauthScopeCache(String appKey, String code) {
        Object object = redisHelper.get(getScopeCacheKey(appKey, code));
        if (Objects.isNull(object)) {
            return "";
        }
        return object.toString();
    }

    /**
     * 第三方登录
     *
     * @param platform 平台
     * @param code     Code
     * @return 登录成功的用户
     */
    public UserEntity thirdLogin(String platform, String code) {
        OauthPlatform oauthPlatform = getOauthPlatform(platform);
        AbstractOauthCallback oauthCallback = getOauthCallbackInstance(oauthPlatform);
        OauthUserInfo userInfo = oauthCallback.getUserInfo(code);
        List<UserThirdLoginEntity> exists = userThirdLoginService.filter(new UserThirdLoginEntity()
                .setPlatform(oauthPlatform.getKey())
                .setThirdUserId(userInfo.getUserId())
        );
        FORBIDDEN.when(exists.isEmpty(), "该第三方账号暂未绑定，无法登录");
        return exists.get(0).getUser();
    }

    /**
     * 第三方绑定
     *
     * @param platform 平台
     * @param code     Code
     * @param user     绑定用户
     */
    public void thirdBind(String platform, String code, UserEntity user) {
        OauthPlatform oauthPlatform = getOauthPlatform(platform);
        AbstractOauthCallback oauthCallback = getOauthCallbackInstance(oauthPlatform);
        OauthUserInfo userInfo = oauthCallback.getUserInfo(code);
        List<UserThirdLoginEntity> exists = userThirdLoginService.filter(new UserThirdLoginEntity()
                .setPlatform(oauthPlatform.getKey())
                .setThirdUserId(userInfo.getUserId())
        );
        exists.forEach(item -> userThirdLoginService.delete(item.getId()));
        int gender = UserGender.FEMALE.getKey();
        if (Objects.nonNull(userInfo.getGender())) {
            gender = userInfo.getGender().getKey();
        }
        userThirdLoginService.add(new UserThirdLoginEntity().setThirdUserId(userInfo.getUserId())
                .setUser(user)
                .setNickName(StringUtils.hasText(userInfo.getNickName()) ? userInfo.getNickName() : "")
                .setAvatar(StringUtils.hasText(userInfo.getAvatar()) ? userInfo.getAvatar() : "")
                .setPlatform(oauthPlatform.getKey())
                .setGender(gender)
        );
    }
}
