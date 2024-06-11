package cn.hamm.spms.module.system.oauth2;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.config.Configs;
import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.open.app.IOpenAppAction;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("oauth2")
@Slf4j
public class Oauth2Controller extends RootController implements IOpenAppAction {
    private static final String APP_KEY = "appKey";
    private static final String INVALID_APP_KEY = "Invalid appKey!";
    private static final String APP_NOT_FOUND = "App(%s) not found!";
    private static final String REDIRECT_URI = "redirectUri";
    private static final String REDIRECT_URI_MISSING = "RedirectUri missing!";

    @GetMapping("authorize")
    public ModelAndView index(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String appKey = request.getParameter(APP_KEY);
        if (!StringUtils.hasText(appKey)) {
            return showError(INVALID_APP_KEY);
        }
        OpenAppEntity openApp;
        try {
            openApp = Services.getOpenAppService().getByAppKey(appKey);
        } catch (java.lang.Exception exception) {
            return showError(String.format(APP_NOT_FOUND, appKey));
        }
        String redirectUri = request.getParameter(REDIRECT_URI);
        if (!StringUtils.hasText(redirectUri)) {
            return showError(REDIRECT_URI_MISSING);
        }
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            // 没有cookie
            return redirectLogin(response, appKey, redirectUri);
        }
        String cookieString = null;
        for (Cookie c : cookies) {
            if (Configs.getCookieConfig().getAuthCookieName().equals(c.getName())) {
                cookieString = c.getValue();
                break;
            }
        }
        if (!StringUtils.hasText(cookieString)) {
            // 没有cookie
            return redirectLogin(response, appKey, redirectUri);
        }
        Long userId = Services.getUserService().getUserIdByCookie(cookieString);
        if (Objects.isNull(userId)) {
            // cookie没有找到用户
            return redirectLogin(response, appKey, redirectUri);
        }
        UserEntity userEntity = Services.getUserService().get(userId);
        String code = Utils.getRandomUtil().randomString();
        openApp.setCode(code).setAppKey(appKey);
        Services.getUserService().saveOauthCode(userEntity.getId(), openApp);
        String redirectTarget;
        redirectTarget = URLDecoder.decode(redirectUri, Charset.defaultCharset());
        String querySplit = "?";
        if (redirectTarget.contains(querySplit)) {
            redirectTarget += "&code=" + code;
        } else {
            redirectTarget += "?code=" + code;
        }
        redirect(response, redirectTarget);
        return null;
    }

    @Description("Code换取AccessToken")
    @Permission(login = false)
    @RequestMapping("accessToken")
    public Json accessToken(@RequestBody @Validated(WhenCode2AccessToken.class) OpenAppEntity openApp) {
        String code = openApp.getCode();
        Long userId = Services.getUserService().getUserIdByOauthAppKeyAndCode(openApp.getAppKey(), code);
        OpenAppEntity existApp = Services.getOpenAppService().getByAppKey(openApp.getAppKey());
        ServiceError.FORBIDDEN.whenNotEquals(existApp.getAppSecret(), openApp.getAppSecret(), "应用秘钥错误");
        Services.getUserService().removeOauthCode(existApp.getAppKey(), code);
        String accessToken = Utils.getSecurityUtil().createAccessToken(userId);
        return Json.data(accessToken);
    }

    private @Nullable ModelAndView redirectLogin(HttpServletResponse response, String appKey, String redirectUri) {
        String url = Services.getAppConfig().getLoginUrl() +
                "?appKey=" +
                appKey +
                "&redirectUri=" +
                redirectUri;
        redirect(response, url);
        return null;
    }

    /**
     * <h2>显示一个错误页面</h2>
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
     * <h2>重定向到指定的URL</h2>
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
}
