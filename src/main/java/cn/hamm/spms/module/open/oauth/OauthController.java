package cn.hamm.spms.module.open.oauth;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.RandomUtil;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.DesensitizeIgnore;
import cn.hamm.airpower.web.access.AccessConfig;
import cn.hamm.airpower.web.access.AccessTokenUtil;
import cn.hamm.airpower.web.access.Permission;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.ApiController;
import cn.hamm.airpower.web.cookie.CookieConfig;
import cn.hamm.airpower.web.curd.ICurdAction;
import cn.hamm.airpower.web.util.RequestUtil;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import cn.hamm.spms.module.open.app.OpenAppService;
import cn.hamm.spms.module.open.oauth.model.enums.OauthScope;
import cn.hamm.spms.module.open.oauth.model.request.OauthCallbackRequest;
import cn.hamm.spms.module.open.oauth.model.request.OauthCreateCodeRequest;
import cn.hamm.spms.module.open.oauth.model.request.OauthGetAccessTokenRequest;
import cn.hamm.spms.module.open.oauth.model.request.OauthGetUserInfoRequest;
import cn.hamm.spms.module.open.oauth.model.response.OauthGetAccessTokenResponse;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginEntity;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.personnel.user.enums.UserTokenType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hamm.airpower.core.DateTimeUtil.SECOND_PER_DAY;
import static cn.hamm.airpower.core.DateTimeUtil.SECOND_PER_HOUR;
import static cn.hamm.airpower.web.exception.ServiceError.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("oauth2")
@Slf4j
public class OauthController extends ApiController implements IOauthAction {
    /**
     * {@code Error}
     */
    public static final String STRING_ERROR = "error";

    private static final String USER_ID = "userId";
    private static final String APP_NOT_FOUND = "App(%s) not found!";
    private static final String REDIRECT_URI = "redirectUri";
    private static final String REDIRECT_URI_MISSING = "RedirectUri missing!";
    private static final String INVALID_APPKEY = "Invalid appKey!";
    private static final String APP_KEY = "appKey";
    private static final String SCOPE = "scope";
    private static final String SCOPE_DELIMITER = ",";

    @Autowired
    private CookieConfig cookieConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private OpenAppService openAppService;

    @Autowired
    private OauthService service;

    @Autowired
    private UserThirdLoginService userThirdLoginService;

    @Autowired
    private AccessConfig accessConfig;

    @Autowired
    private AppConfig appConfig;

    @GetMapping("authorize")
    public ModelAndView index(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String appKey = request.getParameter(APP_KEY);
        if (!StringUtils.hasText(appKey)) {
            return showError(INVALID_APPKEY);
        }
        OpenAppEntity openApp = openAppService.getByAppKey(appKey);
        if (Objects.isNull(openApp)) {
            return showError(String.format(APP_NOT_FOUND, appKey));
        }
        String redirectUri = request.getParameter(REDIRECT_URI);
        if (!StringUtils.hasText(redirectUri)) {
            return showError(REDIRECT_URI_MISSING);
        }
        String scope = getScopeFromRequest(request);
        Long userId = getUserIdFromCookie();
        if (Objects.isNull(userId)) {
            return redirectLogin(response, appKey, redirectUri, scope);
        }
        if (openApp.getIsInternal()) {
            // 内部应用直接返回 code
            redirectToThirdPlatform(response, openApp.getAppKey(), userId, scope, redirectUri);
            return null;
        }
        // 外部应用需要用户确认授权
        Map<String, Object> params = Map.of(
                APP_KEY, appKey,
                REDIRECT_URI, URLEncoder.encode(redirectUri, UTF_8),
                SCOPE, scope
        );
        redirect(response, RequestUtil.buildQueryUrl(appConfig.getLoginUrl(), params));
        return null;
    }

    @Description("获取 AccessToken")
    @Permission(login = false)
    @PostMapping("accessToken")
    public Json accessToken(@RequestBody @Validated({OauthGetAccessTokenRequest.WhenGetAccessToken.class, WhenAppKeyRequired.class}) OauthGetAccessTokenRequest request) {
        // 获取 Code 所属的用户 ID
        Long userId = service.getOauthUserCache(request.getAppKey(), request.getCode());
        // 查询 App 信息
        OpenAppEntity existApp = openAppService.getByAppKey(request.getAppKey());
        FORBIDDEN.whenNotEquals(existApp.getAppSecret(), request.getAppSecret(), "应用秘钥错误");
        // 移除缓存的用户
        service.removeOauthUserCache(existApp.getAppKey(), request.getCode());
        // 获取 Scope
        String scope = service.getOauthScopeCache(request.getAppKey(), request.getCode());
        if (!StringUtils.hasText(scope)) {
            scope = OauthScope.BASIC_INFO.name();
        }
        service.removeOauthScopeCache(existApp.getAppKey(), request.getCode());
        // 生成 accessToken refreshToken
        int expiresIn = SECOND_PER_HOUR * 2;
        String accessToken = buildToken(userId, scope, existApp.getAppKey(), expiresIn);
        String refreshToken = buildToken(userId, scope, existApp.getAppKey(), (long) SECOND_PER_DAY * 30);

        OauthGetAccessTokenResponse response = new OauthGetAccessTokenResponse()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setScope(scope)
                .setExpiresIn((long) expiresIn);
        return Json.data(response);
    }

    @PostMapping("callback")
    @Permission(login = false)
    public Json callback(@RequestBody @Validated(OauthCallbackRequest.WhenOauthCallback.class) OauthCallbackRequest request, HttpServletResponse response) {
        UserEntity user = service.thirdLogin(request.getPlatform(), request.getCode());

        return Json.data(userService.loginWithCookieAndResponse(response, user), "登录成功");
    }

    @PostMapping("thirdBind")
    public Json thirdBind(@RequestBody @Validated(OauthCallbackRequest.WhenOauthCallback.class) OauthCallbackRequest request) {
        UserEntity user = userService.get(getCurrentUserId());
        service.thirdBind(request.getPlatform(), request.getCode(), user);
        return Json.success("绑定成功");
    }

    @PostMapping("unBindThird")
    public Json unBindThird(@RequestBody @Validated(ICurdAction.WhenIdRequired.class) UserThirdLoginEntity userThirdLogin) {
        userThirdLoginService.delete(userThirdLogin.getId());
        return Json.success("解绑成功");
    }

    @Description("获取当前用户的信息")
    @Permission(login = false)
    @PostMapping("getUserInfo")
    @DesensitizeIgnore
    public Json getUserInfo(@RequestBody @Validated(WhenAccessTokenRequired.class) OauthGetUserInfoRequest request) {
        AccessTokenUtil.VerifiedToken verify = AccessTokenUtil.create().verify(request.getAccessToken(), accessConfig.getAccessTokenSecret());
        long userId = Long.parseLong(Objects.requireNonNull(verify.getPayload(USER_ID), "无效的 UserId").toString());
        UserEntity user = userService.get(userId);
        String appKey = Objects.requireNonNull(verify.getPayload(APP_KEY), "无效的 AppKey").toString();
        OpenAppEntity existApp = openAppService.getByAppKey(appKey);
        FORBIDDEN.whenNull(existApp, "应用信息异常");
        String scope = Objects.requireNonNull(verify.getPayload(SCOPE), "无效的 Scope").toString();
        List<String> scopeList = Arrays.stream(scope.split(SCOPE_DELIMITER)).toList();
        OauthScope[] oauthScopes = OauthScope.values();
        for (OauthScope oauthScope : oauthScopes) {
            if (scopeList.contains(oauthScope.name())) {
                // 被授权 跳过
                continue;
            }
            if (OauthScope.CONTACT.equals(oauthScope)) {
                user.setPhone(null).setEmail(null);
            }
            if (OauthScope.PRIVACY.equals(oauthScope)) {
                user.setGender(null).setCreateTime(null).setUpdateTime(null).setIsDisabled(null);
            }
            if (OauthScope.REAL_NAME.equals(oauthScope)) {
                user.setIdCard(null).setRealName(null);
            }
        }
        return Json.data(user);
    }

    @PostMapping("getScopeList")
    public Json getScopeList() {
        return Json.data(DictionaryUtil.getDictionaryList(OauthScope.class,
                OauthScope::name,
                OauthScope::getKey,
                OauthScope::getLabel,
                OauthScope::getDescription,
                OauthScope::getIsDefault
        ));
    }

    @Description("创建 Code")
    @Permission(authorize = false)
    @PostMapping("createCode")
    public Json createCode(@RequestBody @Validated({WhenAppKeyRequired.class, OauthCreateCodeRequest.WhenCreateCode.class}) OauthCreateCodeRequest request) {
        OpenAppEntity openApp = openAppService.getByAppKey(request.getAppKey());
        INVALID_APP_KEY.whenNull(openApp, "AppKey 无效");
        String[] scopes = request.getScope().split(SCOPE_DELIMITER);
        List<String> scopeList = new ArrayList<>();
        PARAM_INVALID.when(scopes.length == 0, "授权范围无效");
        OauthScope[] oauthScopes = OauthScope.values();

        for (OauthScope oauthScope : oauthScopes) {
            if (Arrays.asList(scopes).contains(oauthScope.name()) || oauthScope.getIsDefault()) {
                scopeList.add(oauthScope.name());
            }
        }
        String code = RandomUtil.randomString();
        service.saveOauthUserCache(openApp.getAppKey(), code, getCurrentUserId());
        service.saveOauthScopeCache(openApp.getAppKey(), code, String.join(SCOPE_DELIMITER, scopeList));
        return Json.data(code);
    }

    /**
     * 生成 Token
     *
     * @param userId    用户 ID
     * @param scope     权限
     * @param appKey    App Key
     * @param expiresIn 过期时间(秒)
     * @return Token
     */
    private String buildToken(long userId, String scope, String appKey, long expiresIn) {
        return AccessTokenUtil.create()
                .addPayload(USER_ID, userId)
                .addPayload(SCOPE, scope)
                .addPayload(APP_KEY, appKey)
                .addPayload(UserTokenType.TYPE, UserTokenType.OAUTH2.getKey())
                .setExpireSecond(expiresIn)
                .build(accessConfig.getAccessTokenSecret());
    }

    /**
     * 重定向到登录页面
     *
     * @param response    响应对象
     * @param appKey      AppKey
     * @param redirectUri 重定向地址
     * @param scope       授权范围
     * @return 无返回
     */
    private @Nullable ModelAndView redirectLogin(HttpServletResponse response, String appKey, String redirectUri, String scope) {
        String url = appConfig.getLoginUrl() + "?appKey=" +
                appKey +
                "&redirectUri=" +
                URLEncoder.encode(redirectUri, UTF_8)
                + "&scope=" + URLEncoder.encode(scope, UTF_8);
        redirect(response, url);
        return null;
    }

    /**
     * 显示一个错误页面
     *
     * @param error 错误信息
     * @return 错误页面
     */
    private @NotNull ModelAndView showError(String error) {
        ModelAndView view = new ModelAndView(STRING_ERROR);
        view.getModel().put(STRING_ERROR, error);
        return view;
    }

    /**
     * 重定向到指定的 URL
     *
     * @param response 响应体
     * @param url      目标 URL
     */
    private void redirect(@NotNull HttpServletResponse response, String url) {
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 从 Cookie 获取用户ID
     *
     * @return Cookie 字符串
     */
    private @Nullable Long getUserIdFromCookie() {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            // 没有 Cookie
            return null;
        }
        String cookieString = Arrays.stream(cookies)
                .filter(cookie -> Objects.equals(cookieConfig.getAuthCookieName(), cookie.getName()))
                .findFirst().map(Cookie::getValue)
                .orElse(null);
        if (!StringUtils.hasText(cookieString)) {
            return null;
        }
        Long userId = userService.getUserIdByCookie(cookieString);
        if (Objects.isNull(userId)) {
            // Cookie 没有找到用户
            return null;
        }
        return userId;
    }

    /**
     * 获取权限范围
     *
     * @param request 请求
     * @return 权限字符串
     */
    private @NotNull String getScopeFromRequest(@NotNull HttpServletRequest request) {
        String scope = request.getParameter(SCOPE);
        if (!StringUtils.hasText(scope)) {
            scope = Arrays.stream(OauthScope.values())
                    .filter(OauthScope::getIsDefault)
                    .map(Enum::name)
                    .collect(Collectors.joining(SCOPE_DELIMITER));
        }
        return scope;
    }

    /**
     * 重定向回第三方页面
     *
     * @param response    响应
     * @param appKey      appKey
     * @param userId      用户 ID
     * @param scope       权限列表
     * @param redirectUri 第三方回调地址
     */
    private void redirectToThirdPlatform(HttpServletResponse response, String appKey, Long userId, String scope, String redirectUri) {
        String code = RandomUtil.randomString();
        service.saveOauthUserCache(appKey, code, userId);
        service.saveOauthScopeCache(appKey, code, scope);
        String url = RequestUtil.buildQueryUrl(redirectUri, Map.of("code", code));
        redirect(response, url);
    }
}
