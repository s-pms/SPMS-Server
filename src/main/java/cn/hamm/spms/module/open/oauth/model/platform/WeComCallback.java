package cn.hamm.spms.module.open.oauth.model.platform;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.helper.AirHelper;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.HttpUtil;
import cn.hamm.spms.module.open.oauth.OauthConfig;
import cn.hamm.spms.module.open.oauth.model.base.AbstractOauthCallback;
import cn.hamm.spms.module.open.oauth.model.base.OauthUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.springframework.util.StringUtils;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;

/**
 * <h1>企业微信回调</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
public class WeComCallback extends AbstractOauthCallback {
    private static final String ACCESS_TOKEN_CACHE_KEY = "wecom_token_";
    private static final String ACCESS_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";
    private static final String USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=%s&code=%s";

    @Override
    public OauthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken();
        String url = String.format(USER_INFO_URL, accessToken, code);
        HttpResponse<String> httpResponse = HttpUtil.create().setUrl(url).get();
        Map<String, Object> map = Json.parse2Map(httpResponse.body());
        Object userId = map.get("UserId");
        FORBIDDEN.whenNull(userId, "获取用户ID失败");
        return new OauthUserInfo().setUserId(userId.toString());
    }

    @Contract(pure = true)
    private String getAccessToken() {
        Object object = AirHelper.getRedisHelper().get(ACCESS_TOKEN_CACHE_KEY);
        if (Objects.nonNull(object)) {
            return object.toString();
        }
        String url = String.format(ACCESS_TOKEN_URL, OauthConfig.getWecomConfig().getCorpId(), OauthConfig.getWecomConfig().getCorpSecret());
        HttpResponse<String> httpResponse = HttpUtil.create().setUrl(url).get();
        Map<String, Object> map = Json.parse2Map(httpResponse.body());
        Object accessToken = Objects.requireNonNull(map.get("access_token"), "AccessToken获取失败");
        FORBIDDEN.when(!StringUtils.hasText(accessToken.toString()), "AccessToken获取失败");
        AirHelper.getRedisHelper().set(ACCESS_TOKEN_CACHE_KEY, accessToken.toString(), Constant.SECOND_PER_HOUR);
        log.info("企业微信获取AccessToken: {}", accessToken);
        return accessToken.toString();
    }
}
