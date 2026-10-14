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

import static cn.hamm.airpower.exception.Errors.DATA_NOT_FOUND;
import static cn.hamm.airpower.exception.Errors.FORBIDDEN;

/**
 * <h1>第三方授权</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class OauthService {
    /**
     * 授权码缓存时长：5 分钟
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
        return "oauth:" + appKey + ":" + code + ":user:";
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
        return "oauth:" + appKey + ":" + code + ":scope:";
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
     * 缓存授权码对应的用户 ID
     *
     * @param appKey AppKey
     * @param code   授权码
     * @param userId 用户 ID
     */
    public void saveOauthUserCache(String appKey, String code, long userId) {
        redisHelper.set(getUserIdCacheKey(appKey, code), userId, CACHE_CODE_EXPIRE_SECOND);
    }

    /**
     * 读取授权码对应的用户 ID
     *
     * @param appKey AppKey
     * @param code   授权码
     * @return 用户 ID
     * @apiNote 不抛「未找到」，直接 FORBIDDEN，避免通过报错差异探测授权码是否存在
     */
    public Long getOauthUserCache(String appKey, String code) {
        Object userId = redisHelper.get(getUserIdCacheKey(appKey, code));
        FORBIDDEN.whenNull(userId, "你的 AppKey 或 Code 错误，请重新获取");
        return Long.valueOf(userId.toString());
    }

    /**
     * 删除授权码对应的用户 ID
     *
     * @param appKey AppKey
     * @param code   授权码
     */
    public void removeOauthUserCache(String appKey, String code) {
        redisHelper.delete(getUserIdCacheKey(appKey, code));
    }

    /**
     * 删除授权码对应的授权范围
     *
     * @param appKey AppKey
     * @param code   授权码
     */
    public void removeOauthScopeCache(String appKey, String code) {
        redisHelper.delete(getScopeCacheKey(appKey, code));
    }

    /**
     * 缓存授权码对应的授权范围
     *
     * @param appKey AppKey
     * @param code   授权码
     * @param scope  授权范围
     */
    public void saveOauthScopeCache(String appKey, String code, String scope) {
        redisHelper.set(getScopeCacheKey(appKey, code), scope, CACHE_CODE_EXPIRE_SECOND);
    }

    /**
     * 读取授权码对应的授权范围
     *
     * @param appKey AppKey
     * @param code   授权码
     * @return 授权范围，未缓存时返回空串
     */
    public String getOauthScopeCache(String appKey, String code) {
        Object object = redisHelper.get(getScopeCacheKey(appKey, code));
        if (Objects.isNull(object)) {
            return "";
        }
        return object.toString();
    }

    /**
     * 用第三方授权码登录
     *
     * @param platform 平台标识，取 {@link OauthPlatform#getFlag()}
     * @param code     第三方临时授权码
     * @return 登录的用户
     * @apiNote 只认已绑定的第三方账号，未绑定一律拒绝，不做「找不到就自动注册」——
     * 否则任何人拿到一个 code 都能在本系统开户
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
     * 把第三方账号绑定到当前用户
     *
     * @param platform 平台标识，取 {@link OauthPlatform#getFlag()}
     * @param code     第三方临时授权码
     * @param user     绑定到的用户
     * @apiNote 先删掉该第三方账号的旧绑定再新增：同一第三方账号全局只允许绑一个用户，
     * 换绑必须走这里，否则会在两个用户间同时有效
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
