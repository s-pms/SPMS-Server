package cn.hamm.spms.module.open.oauth.model.platform;

import cn.hamm.airpower.web.api.Json;
import cn.hamm.airpower.web.datetime.DateTimeUtil;
import cn.hamm.airpower.web.redis.RedisHelper;
import cn.hamm.airpower.web.request.HttpUtil;
import cn.hamm.spms.module.open.oauth.config.WecomConfig;
import cn.hamm.spms.module.open.oauth.model.base.AbstractOauthCallback;
import cn.hamm.spms.module.open.oauth.model.base.OauthUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN;

/**
 * <h1>企业微信回调</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Component
public class WeComCallback extends AbstractOauthCallback {
    private static final String ACCESS_TOKEN_CACHE_KEY = "wecom_token_";
    private static final String ACCESS_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";
    private static final String USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=%s&code=%s";
    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private WecomConfig wecomConfig;

    @Override
    public OauthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken();
        String url = String.format(USER_INFO_URL, accessToken, code);
        HttpResponse<String> httpResponse = HttpUtil.create().setUrl(url).get();
        Map<String, Object> map = Json.parse2Map(httpResponse.body());
        Object userId = map.get("UserId");
        FORBIDDEN.whenNull(userId, "获取用户 ID 失败");
        return new OauthUserInfo().setUserId(userId.toString());
    }

    @Contract(pure = true)
    private String getAccessToken() {
        Object object = redisHelper.get(ACCESS_TOKEN_CACHE_KEY);
        if (Objects.nonNull(object)) {
            return object.toString();
        }
        String url = String.format(ACCESS_TOKEN_URL, wecomConfig.getCorpId(), wecomConfig.getCorpSecret());
        HttpResponse<String> httpResponse = HttpUtil.create().setUrl(url).get();
        Map<String, Object> map = Json.parse2Map(httpResponse.body());
        Object accessToken = Objects.requireNonNull(map.get("access_token"), "AccessToken 获取失败");
        FORBIDDEN.when(!StringUtils.hasText(accessToken.toString()), "AccessToken 获取失败");
        redisHelper.set(ACCESS_TOKEN_CACHE_KEY, accessToken.toString(), DateTimeUtil.SECOND_PER_HOUR);
        log.info("企业微信获取AccessToken: {}", accessToken);
        return accessToken.toString();
    }
}
