package cn.hamm.spms.module.system.oauth2;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.config.AirConfig;
import cn.hamm.airpower.enums.Result;
import cn.hamm.airpower.model.json.JsonData;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.util.AirUtil;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.system.app.AppEntity;
import cn.hamm.spms.module.system.app.AppService;
import cn.hamm.spms.module.system.app.IAppAction;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
@RestController
@RequestMapping("oauth2")
@Slf4j
public class Oauth2Controller extends RootController implements IAppAction {
    @Autowired
    private UserService userService;

    @Autowired
    private AppService appService;

    @Autowired
    private AppConfig appConfig;

    @GetMapping("authorize")
    public ModelAndView index(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String appKey = request.getParameter("appKey");
        if (!StringUtils.hasText(appKey)) {
            return showError("Invalid appKey!");
        }
        AppEntity appEntity;
        try {
            appEntity = appService.getByAppKey(appKey);
        } catch (Exception exception) {
            return showError("App(" + appKey + ") not found!");
        }
        String redirectUri = request.getParameter("redirectUri");
        if (!StringUtils.hasText(redirectUri)) {
            return showError("RedirectUri missing!");
        }
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            // 没有cookie
            return redirectLogin(response, appKey, redirectUri);
        }
        String cookieString = null;
        for (Cookie c : cookies) {
            if (AirConfig.getCookieConfig().getAuthCookieName().equals(c.getName())) {
                cookieString = c.getValue();
                break;
            }
        }
        if (!StringUtils.hasText(cookieString)) {
            // 没有cookie
            return redirectLogin(response, appKey, redirectUri);
        }
        Long userId = userService.getUserIdByCookie(cookieString);
        if (Objects.isNull(userId)) {
            // cookie没有找到用户
            return redirectLogin(response, appKey, redirectUri);
        }
        UserEntity userEntity = userService.get(userId);
        String code = AirUtil.getRandomUtil().randomString();
        appEntity.setCode(code).setAppKey(appKey);
        userService.saveOauthCode(userEntity.getId(), appEntity);
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
    public JsonData accessToken(@RequestBody @Validated(WhenCode2AccessToken.class) AppEntity appEntity) {
        String code = appEntity.getCode();
        Long userId = userService.getUserIdByOauthAppKeyAndCode(appEntity.getAppKey(), code);
        AppEntity existApp = appService.getByAppKey(appEntity.getAppKey());
        Result.FORBIDDEN.whenNotEquals(existApp.getAppSecret(), appEntity.getAppSecret(), "应用秘钥错误");
        userService.removeOauthCode(existApp.getAppKey(), code);
        String accessToken = AirUtil.getSecurityUtil().createAccessToken(userId);
        return new JsonData(accessToken);
    }

    private @Nullable ModelAndView redirectLogin(HttpServletResponse response, String appKey, String redirectUri) {
        String url = appConfig.getLoginUrl() +
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
        ModelAndView view = new ModelAndView("error");
        view.getModel().put("error", error);
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
