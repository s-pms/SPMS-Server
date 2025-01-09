package cn.hamm.spms.module.open.oauth;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.DesensitizeExclude;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.config.CookieConfig;
import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.interfaces.IEntityAction;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.util.AccessTokenUtil;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import cn.hamm.spms.module.open.app.OpenAppService;
import cn.hamm.spms.module.open.oauth.model.enums.OauthScope;
import cn.hamm.spms.module.open.oauth.model.request.OauthCallbackRequest;
import cn.hamm.spms.module.open.oauth.model.request.OauthCreateCodeRequest;
import cn.hamm.spms.module.open.oauth.model.request.OauthGetAccessTokenRequest;
import cn.hamm.spms.module.open.oauth.model.request.OauthGetUserInfoRequest;
import cn.hamm.spms.module.open.oauth.model.response.OauthGetAccessTokenResponse;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.personnel.user.thirdlogin.UserThirdLoginEntity;
import cn.hamm.spms.module.personnel.user.thirdlogin.UserThirdLoginService;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("oauth2")
@Slf4j
public class OauthController extends RootController implements IOauthAction {
    private static final String USER_ID = "userId";
    private static final String APP_NOT_FOUND = "App(%s) not found!";
    private static final String REDIRECT_URI = "redirectUri";
    private static final String REDIRECT_URI_MISSING = "RedirectUri missing!";
    private static final String INVALID_APP_KEY = "Invalid appKey!";
    private static final String APP_KEY = "appKey";
    private static final String SCOPE = "scope";

    @Autowired
    private CookieConfig cookieConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private OpenAppService openAppService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private OauthService service;

    @Autowired
    private UserThirdLoginService userThirdLoginService;

    @GetMapping("authorize")
    public ModelAndView index(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String appKey = request.getParameter(APP_KEY);
        if (!StringUtils.hasText(appKey)) {
            return showError(INVALID_APP_KEY);
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
            // 内部应用直接返回code
            redirectToThirdPlatform(response, openApp.getAppKey(), userId, scope, redirectUri);
            return null;
        }
        // 外部应用需要用户确认授权
        String url = appConfig.getAuthorizeUrl() +
                Constant.QUESTION +
                APP_KEY +
                Constant.EQUAL +
                appKey +
                Constant.AND +
                REDIRECT_URI +
                Constant.EQUAL +
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                Constant.AND +
                SCOPE +
                Constant.EQUAL +
                scope;

        redirect(response, url);
        return null;
    }

    @Description("获取AccessToken")
    @Permission(login = false)
    @PostMapping("accessToken")
    public Json accessToken(@RequestBody @Validated({OauthGetAccessTokenRequest.WhenGetAccessToken.class, WhenAppKeyRequired.class}) OauthGetAccessTokenRequest request) {
        // 获取Code所属的用户ID
        Long userId = service.getOauthUserCache(request.getAppKey(), request.getCode());
        // 查询App信息
        OpenAppEntity existApp = openAppService.getByAppKey(request.getAppKey());
        ServiceError.FORBIDDEN.whenNotEquals(existApp.getAppSecret(), request.getAppSecret(), "应用秘钥错误");
        // 移除缓存的用户
        service.removeOauthUserCache(existApp.getAppKey(), request.getCode());
        // 获取Scope
        String scope = service.getOauthScopeCache(request.getAppKey(), request.getCode());
        if (!StringUtils.hasText(scope)) {
            scope = OauthScope.BASIC_INFO.name();
        }
        service.removeOauthScopeCache(existApp.getAppKey(), request.getCode());
        // 生成 accessToken refreshToken
        int expiresIn = Constant.MILLISECONDS_PER_SECOND * Constant.SECOND_PER_HOUR * 2;
        String accessToken = buildToken(userId, scope, existApp.getAppKey(), expiresIn);
        String refreshToken = buildToken(userId, scope, existApp.getAppKey(), (long) Constant.MILLISECONDS_PER_SECOND * Constant.SECOND_PER_DAY * 30);

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
    public Json unBindThird(@RequestBody @Validated(IEntityAction.WhenIdRequired.class) UserThirdLoginEntity userThirdLogin) {
        userThirdLoginService.delete(userThirdLogin.getId());
        return Json.success("解绑成功");
    }

    @Description("获取当前用户的信息")
    @Permission(login = false)
    @PostMapping("getUserInfo")
    @DesensitizeExclude
    public Json getUserInfo(@RequestBody @Validated(WhenAccessTokenRequired.class) OauthGetUserInfoRequest request) {
        AccessTokenUtil.VerifiedToken verify = AccessTokenUtil.create().verify(request.getAccessToken(), serviceConfig.getAccessTokenSecret());
        long userId = Long.parseLong(Objects.requireNonNull(verify.getPayload(USER_ID), "无效的UserId").toString());
        UserEntity user = userService.get(userId);
        String appKey = Objects.requireNonNull(verify.getPayload(APP_KEY), "无效的AppKey").toString();
        OpenAppEntity existApp = openAppService.getByAppKey(appKey);
        ServiceError.FORBIDDEN.whenNull(existApp, "应用信息异常");
        String scope = Objects.requireNonNull(verify.getPayload(SCOPE), "无效的Scope").toString();
        List<String> scopeList = Arrays.stream(scope.split(Constant.COMMA)).toList();
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
        OauthScope[] oauthScopes = OauthScope.values();
        List<Map<String, Object>> list = Arrays.stream(oauthScopes).<Map<String, Object>>map(oauthScope -> Map.of(
                "key", oauthScope.getKey(),
                "name", oauthScope.name(),
                "label", oauthScope.getLabel(),
                "description", oauthScope.getDescription(),
                "isDefault", oauthScope.getIsDefault()
        )).collect(Collectors.toList());
        return Json.data(list);
    }

    @Description("创建Code")
    @Permission(authorize = false)
    @PostMapping("createCode")
    public Json createCode(@RequestBody @Validated({WhenAppKeyRequired.class, OauthCreateCodeRequest.WhenCreateCode.class}) OauthCreateCodeRequest request) {
        OpenAppEntity openApp = openAppService.getByAppKey(request.getAppKey());
        ServiceError.INVALID_APP_KEY.whenNull(openApp, "AppKey无效");
        String[] scopes = request.getScope().split(Constant.COMMA);
        List<String> scopeList = new ArrayList<>();
        ServiceError.PARAM_INVALID.when(scopes.length == 0, "授权范围无效");
        OauthScope[] oauthScopes = OauthScope.values();

        for (OauthScope oauthScope : oauthScopes) {
            if (Arrays.asList(scopes).contains(oauthScope.name()) || oauthScope.getIsDefault()) {
                scopeList.add(oauthScope.name());
            }
        }
        String code = RandomUtil.randomString();
        service.saveOauthUserCache(openApp.getAppKey(), code, getCurrentUserId());
        service.saveOauthScopeCache(openApp.getAppKey(), code, String.join(Constant.COMMA, scopeList));
        return Json.data(code);
    }

    /**
     * <h3>生成Token</h3>
     *
     * @param userId    用户ID
     * @param scope     权限
     * @param appKey    App Key
     * @param expiresIn 过期时间
     * @return Token
     */
    private String buildToken(long userId, String scope, String appKey, long expiresIn) {
        return AccessTokenUtil.create()
                .addPayload(USER_ID, userId)
                .addPayload(SCOPE, scope)
                .addPayload(APP_KEY, appKey)
                .setExpireMillisecond(expiresIn)
                .build(serviceConfig.getAccessTokenSecret());
    }

    /**
     * <h3>重定向到登录页面</h3>
     *
     * @param response    响应对象
     * @param appKey      AppKey
     * @param redirectUri 重定向地址
     * @param scope       授权范围
     * @return 无返回
     */
    private @Nullable ModelAndView redirectLogin(HttpServletResponse response, String appKey, String redirectUri, String scope) {
        String url = appConfig.getLoginUrl() +
                "?appKey=" +
                appKey +
                "&redirectUri=" +
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
        redirect(response, url);
        return null;
    }

    /**
     * <h3>显示一个错误页面</h3>
     *
     * @param error 错误信息
     * @return 错误页面
     */
    private @NotNull ModelAndView showError(String error) {
        ModelAndView view = new ModelAndView(Constant.ERROR);
        view.getModel().put(Constant.ERROR, error);
        return view;
    }

    /**
     * <h3>重定向到指定的URL</h3>
     *
     * @param response 响应体
     * @param url      目标URL
     */
    private void redirect(@NotNull HttpServletResponse response, String url) {
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * <h3>从Cookie获取用户ID</h3>
     *
     * @return Cookie字符串
     */
    private @Nullable Long getUserIdFromCookie() {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            // 没有cookie
            return null;
        }
        String cookieString = Arrays.stream(cookies)
                .filter(c -> Objects.equals(cookieConfig.getAuthCookieName(), c.getName()))
                .findFirst().map(Cookie::getValue)
                .orElse(null);
        if (!StringUtils.hasText(cookieString)) {
            return null;
        }
        Long userId = userService.getUserIdByCookie(cookieString);
        if (Objects.isNull(userId)) {
            // cookie没有找到用户
            return null;
        }
        return userId;
    }

    /**
     * <h3>获取scope</h3>
     *
     * @param request 请求
     * @return scope字符串
     */
    private @NotNull String getScopeFromRequest(@NotNull HttpServletRequest request) {
        String scope = request.getParameter(SCOPE);
        if (!StringUtils.hasText(scope)) {
            scope = Arrays.stream(OauthScope.values())
                    .filter(OauthScope::getIsDefault)
                    .map(Enum::name)
                    .collect(Collectors.joining(Constant.COMMA));
        }
        return scope;
    }

    /**
     * <h3>重定向回第三方页面</h3>
     *
     * @param response    响应
     * @param appKey      appKey
     * @param userId      用户ID
     * @param scope       权限列表
     * @param redirectUri 第三方回调地址
     */
    private void redirectToThirdPlatform(HttpServletResponse response, String appKey, Long userId, String scope, String redirectUri) {
        String code = RandomUtil.randomString();
        service.saveOauthUserCache(appKey, code, userId);
        service.saveOauthScopeCache(appKey, code, scope);
        String url = redirectUri + Constant.QUESTION + Constant.CODE + Constant.EQUAL + code;
        redirect(response, url);
    }
}
