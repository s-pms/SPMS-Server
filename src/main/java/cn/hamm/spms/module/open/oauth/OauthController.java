package cn.hamm.spms.module.open.oauth;

import cn.hamm.airpower.api.ApiController;
import cn.hamm.airpower.api.RequestUtil;
import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.api.config.ApiConfig;
import cn.hamm.airpower.cookie.CookieConfig;
import cn.hamm.airpower.core.AccessTokenUtil;
import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.RandomUtil;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.DesensitizeIgnore;
import cn.hamm.airpower.curd.base.ICurdAction;
import cn.hamm.airpower.curd.permission.Permission;
import cn.hamm.spms.common.AppConfig;
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
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hamm.airpower.core.DateTimeUtil.SECOND_PER_DAY;
import static cn.hamm.airpower.core.DateTimeUtil.SECOND_PER_HOUR;
import static cn.hamm.airpower.exception.Errors.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <h1>第三方授权</h1>
 *
 * @author Hamm.cn
 */
@Api("oauth2")
@Slf4j
@Permission
public class OauthController extends ApiController implements IOauthAction {
    /**
     * OAuth2 错误页视图名
     */
    public static final String STRING_ERROR = "error";

    private static final String USER_ID = "userId";
    private static final String APP_NOT_FOUND = "App(%s) not found!";
    private static final String REDIRECT_URI = "redirectUri";
    private static final String REDIRECT_URI_MISSING = "RedirectUri missing!";
    private static final String REDIRECT_URI_MISMATCH = "回调地址与应用注册地址不匹配!";
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
    private ApiConfig apiConfig;

    @Autowired
    private AppConfig appConfig;

    /**
     * 发起授权，未登录时跳登录页
     *
     * @param request  请求
     * @param response 响应
     * @return 错误页，未发生错误时返回 {@code null}（响应已 302）
     * @apiNote 内部应用跳过用户确认直接发码，外部应用统一跳到登录页由前端渲染授权确认页
     */
    @Permission(login = false)
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
        // 回调地址必须与应用注册地址一致，否则授权码会被送到攻击者站点
        if (!isRedirectUriAllowed(redirectUri, openApp.getUrl())) {
            log.warn("回调地址与应用注册地址不匹配，已拒绝。appKey:{} 请求地址:{} 注册地址:{}",
                    appKey, redirectUri, openApp.getUrl());
            return showError(REDIRECT_URI_MISMATCH);
        }
        String scope = getScopeFromRequest(request);
        Long userId = getUserIdFromCookie();
        if (Objects.isNull(userId)) {
            return redirectLogin(response, appKey, redirectUri, scope);
        }
        if (openApp.getIsInternal()) {
            redirectToThirdPlatform(response, openApp.getAppKey(), userId, scope, redirectUri);
            return null;
        }
        Map<String, Object> params = Map.of(
                APP_KEY, appKey,
                REDIRECT_URI, URLEncoder.encode(redirectUri, UTF_8),
                SCOPE, scope
        );
        redirect(response, RequestUtil.buildQueryUrl(appConfig.getLoginUrl(), params));
        return null;
    }

    /**
     * 用授权码换取 AccessToken
     *
     * @param request 换取请求
     * @return AccessToken、RefreshToken、授权范围与过期时间
     * @apiNote 校验通过后立即删除授权码缓存，一个 code 只能换一次 Token。
     * AccessToken 有效 2 小时、RefreshToken 有效 30 天，但本项目没有刷新接口，
     * 拿到过期 Token 只能重新走一遍授权流程
     */
    @Description("获取 AccessToken")
    @Permission(login = false)
    @PostMapping("accessToken")
    public Json accessToken(@RequestBody @Validated({OauthGetAccessTokenRequest.WhenGetAccessToken.class, WhenAppKeyRequired.class}) OauthGetAccessTokenRequest request) {
        Long userId = service.getOauthUserCache(request.getAppKey(), request.getCode());
        OpenAppEntity existApp = openAppService.getByAppKey(request.getAppKey());
        FORBIDDEN.whenNotEquals(existApp.getAppSecret(), request.getAppSecret(), "应用秘钥错误");
        service.removeOauthUserCache(existApp.getAppKey(), request.getCode());
        String scope = service.getOauthScopeCache(request.getAppKey(), request.getCode());
        if (!StringUtils.hasText(scope)) {
            scope = OauthScope.BASIC_INFO.name();
        }
        service.removeOauthScopeCache(existApp.getAppKey(), request.getCode());
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

    /**
     * 第三方授权码登录
     *
     * @param request  回调请求
     * @param response 响应
     * @return 登录后的 Cookie 与 Token
     */
    @PostMapping("callback")
    @Permission(login = false)
    public Json callback(@RequestBody @Validated(OauthCallbackRequest.WhenOauthCallback.class) OauthCallbackRequest request, HttpServletResponse response) {
        UserEntity user = service.thirdLogin(request.getPlatform(), request.getCode());

        return Json.data(userService.loginWithCookieAndResponse(response, user), "登录成功");
    }

    /**
     * 把当前用户绑定到第三方账号
     *
     * @param request 回调请求
     * @apiNote 该接口会走一次第三方换用户信息，等于让当前登录用户主动把第三方账号交出去，
     * 属于主动换绑场景，不存在未授权风险
     */
    @PostMapping("thirdBind")
    @Permission(authorize = false)
    public Json thirdBind(@RequestBody @Validated(OauthCallbackRequest.WhenOauthCallback.class) OauthCallbackRequest request) {
        UserEntity user = userService.get(getCurrentUserId());
        service.thirdBind(request.getPlatform(), request.getCode(), user);
        return Json.success("绑定成功");
    }

    /**
     * 解绑第三方账号
     *
     * @param userThirdLogin 绑定记录
     * @apiNote 必须校验绑定记录归属当前用户，否则可解别人的绑定
     */
    @PostMapping("unBindThird")
    @Permission(authorize = false)
    public Json unBindThird(@RequestBody @Validated(ICurdAction.WhenIdRequired.class) UserThirdLoginEntity userThirdLogin) {
        UserThirdLoginEntity exist = userThirdLoginService.get(userThirdLogin.getId());
        DATA_NOT_FOUND.whenNull(exist, "解绑失败，数据不存在");
        FORBIDDEN.whenNotEquals(exist.getUser().getId(), getCurrentUserId(), "解绑失败, 你无权操作");
        userThirdLoginService.delete(userThirdLogin.getId());
        return Json.success("解绑成功");
    }

    /**
     * 按 AccessToken 获取用户信息
     *
     * @param request 请求
     * @return 按授权范围裁剪过的用户信息
     * @apiNote 裁剪是「白名单式」的：未授权的 scope 对应字段一律置 null，
     * 但必须逐个枚举补齐，新加字段默认不裁剪就会直接泄露给第三方
     */
    @Description("获取当前用户的信息")
    @Permission(login = false)
    @PostMapping("getUserInfo")
    @DesensitizeIgnore
    public Json getUserInfo(@RequestBody @Validated(WhenAccessTokenRequired.class) OauthGetUserInfoRequest request) {
        AccessTokenUtil.VerifiedToken verify = AccessTokenUtil.create().verify(request.getAccessToken(), apiConfig.getAccessTokenSecret());
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
                continue;
            }
            if (OauthScope.CONTACT.equals(oauthScope)) {
                user.setEmail(null);
            }
            if (OauthScope.PRIVACY.equals(oauthScope)) {
                user.setGender(null).setCreateTime(null).setUpdateTime(null).setIsDisabled(null);
                // 部门与角色属于公司内部组织信息，漏裁就会让任意第三方应用拿到完整组织架构
                user.setRoleList(null).setDepartmentList(null);
            }
            if (OauthScope.REAL_NAME.equals(oauthScope)) {
                user.setIdCard(null).setRealName(null);
            }
        }
        return Json.data(user);
    }

    /**
     * 获取全部可选的授权范围
     *
     * @return 授权范围列表
     */
    @PostMapping("getScopeList")
    @Permission(login = false)
    public Json getScopeList() {
        return Json.data(DictionaryUtil.getDictionaryList(OauthScope.class,
                OauthScope::name,
                OauthScope::getKey,
                OauthScope::getLabel,
                OauthScope::getDescription,
                OauthScope::getIsDefault
        ));
    }

    /**
     * 为应用签发授权码
     *
     * @param request 创建请求
     * @return 授权码
     * @apiNote 授权码与用户 ID、授权范围一起缓存在 Redis，有效期 5 分钟，只能换一次 Token；
     * 标了默认的 scope 会被无条件追加进来，客户端无法拒绝
     */
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
     * 生成 AccessToken
     *
     * @param userId    用户 ID
     * @param scope     授权范围
     * @param appKey    AppKey
     * @param expiresIn 过期时间（秒）
     * @return Token
     * @apiNote Token 是无状态签名，签发与校验用同一把 accessTokenSecret，密钥轮换会让全部已签发 Token 失效
     */
    private String buildToken(long userId, String scope, String appKey, long expiresIn) {
        return AccessTokenUtil.create()
                .addPayload(USER_ID, userId)
                .addPayload(SCOPE, scope)
                .addPayload(APP_KEY, appKey)
                .addPayload(UserTokenType.TYPE, UserTokenType.OAUTH2.getKey())
                .setExpireSecond(expiresIn)
                .build(apiConfig.getAccessTokenSecret());
    }

    /**
     * 重定向到登录页面
     *
     * @param response    响应对象
     * @param appKey      AppKey
     * @param redirectUri 回调地址
     * @param scope       授权范围
     * @return 恒为 {@code null}，已发出 302
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
     * 构造 OAuth2 错误页
     *
     * @param error 错误信息
     * @return 错误页
     */
    private @NotNull ModelAndView showError(String error) {
        ModelAndView view = new ModelAndView(STRING_ERROR);
        view.getModel().put(STRING_ERROR, error);
        return view;
    }

    /**
     * 重定向到指定 URL
     *
     * @param response 响应体
     * @param url      目标 URL
     * @apiNote 重定向失败只记日志。目标 URL 全部来自应用注册地址或本次请求参数，
     * 前者已在 {@link #isRedirectUriAllowed} 校验过
     */
    private void redirect(@NotNull HttpServletResponse response, String url) {
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 从登录 Cookie 中取用户 ID
     *
     * @return 用户 ID，未登录返回 {@code null}
     */
    private @Nullable Long getUserIdFromCookie() {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
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
            return null;
        }
        return userId;
    }

    /**
     * 解析请求中的授权范围
     *
     * @param request 请求
     * @return 授权范围，未指定时取所有默认 scope
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
     * 校验回调地址是否与应用注册地址匹配
     *
     * @param redirectUri   本次请求携带的回调地址
     * @param registeredUrl 应用注册时填写的地址
     * @return 是否允许
     * @apiNote 协议、主机、端口必须完全一致；路径按注册路径做前缀匹配，
     * 注册 {@code https://a.com/callback} 时允许 {@code https://a.com/callback/x}，
     * 但不允许 {@code https://a.com/other} 或 {@code https://evil.com/callback}。
     * 两者都要求绝对地址且不含用户信息段
     */
    private boolean isRedirectUriAllowed(@Nullable String redirectUri, @Nullable String registeredUrl) {
        if (!StringUtils.hasText(redirectUri) || !StringUtils.hasText(registeredUrl)) {
            return false;
        }
        URI requested = parseUri(redirectUri);
        URI registered = parseUri(registeredUrl);
        if (Objects.isNull(requested) || Objects.isNull(registered)) {
            return false;
        }
        // RFC 6454：redirect_uri 不得包含用户信息段，
        // 形如 https://a.com@evil.com/cb 的地址极易在人工审阅时看错真实主机
        if (Objects.nonNull(requested.getUserInfo()) || Objects.nonNull(registered.getUserInfo())) {
            log.warn("回调地址包含用户信息段，已拒绝。请求地址:{} 注册地址:{}", redirectUri, registeredUrl);
            return false;
        }
        if (!Objects.equals(registered.getScheme(), requested.getScheme())
                || !Objects.equals(registered.getHost(), requested.getHost())
                || registered.getPort() != requested.getPort()) {
            return false;
        }
        String registeredPath = normalizePath(registered.getPath());
        String requestedPath = normalizePath(requested.getPath());
        if ("/".equals(registeredPath)) {
            return true;
        }
        // 逐段匹配，避免 /callback 匹配上 /callback-evil
        return requestedPath.equals(registeredPath)
                || requestedPath.startsWith(registeredPath + "/");
    }

    /**
     * 归一化路径：空值视为根路径，去掉末尾多余的斜杠
     *
     * @param path 原始路径
     * @return 归一化后的路径
     */
    private @NotNull String normalizePath(@Nullable String path) {
        if (!StringUtils.hasText(path)) {
            return "/";
        }
        String result = path.trim();
        while (result.length() > 1 && result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 解析 URL，非法格式返回 {@code null}
     *
     * @param url URL 字符串
     * @return 解析结果
     */
    private @Nullable URI parseUri(@NotNull String url) {
        try {
            URI uri = URI.create(url.trim());
            if (!StringUtils.hasText(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException e) {
            log.warn("回调地址格式非法：{}", url);
            return null;
        }
    }

    /**
     * 重定向到第三方页面
     *
     * @param response    响应
     * @param appKey      appKey
     * @param userId      用户 ID
     * @param scope       授权范围
     * @param redirectUri 第三方回调地址
     * @apiNote 授权码与用户、scope 一起写 Redis 后再拼回调 URL，只有换取 Token 时才真正用掉
     */
    private void redirectToThirdPlatform(HttpServletResponse response, String appKey, Long userId, String scope, String redirectUri) {
        String code = RandomUtil.randomString();
        service.saveOauthUserCache(appKey, code, userId);
        service.saveOauthScopeCache(appKey, code, scope);
        String url = RequestUtil.buildQueryUrl(redirectUri, Map.of("code", code));
        redirect(response, url);
    }
}
