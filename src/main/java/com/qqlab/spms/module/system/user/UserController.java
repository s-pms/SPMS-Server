package com.qqlab.spms.module.system.user;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.ResponseFilter;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.CookieUtil;
import cn.hamm.airpower.security.Permission;
import cn.hamm.airpower.security.SecurityUtil;
import com.qqlab.spms.module.system.app.AppEntity;
import com.qqlab.spms.module.system.app.AppService;
import com.qqlab.spms.module.system.app.AppVo;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("user")
@Description("用户")
public class UserController extends RootEntityController<UserService, UserVo> {
    @Autowired
    private AppService appService;

    @Autowired
    private SecurityUtil securityUtil;

    @Description("获取我的信息")
    @Permission(authorize = false)
    @PostMapping("getMyInfo")
    @ResponseFilter(UserEntity.WhenGetMyInfo.class)
    public JsonData getMyInfo(Long userId) {
        return jsonData(service.getById(userId));
    }

    @Description("修改我的信息")
    @Permission(authorize = false)
    @PostMapping("updateMyInfo")
    public Json updateMyInfo(@RequestBody @Validated({UserEntity.WhenUpdateMyInfo.class}) UserEntity userEntity, Long userId) {
        userEntity.setId(userId);
        userEntity.setRoleList(null);
        service.update(userEntity);
        return json("资料修改成功");
    }

    @Description("获取我的菜单")
    @Permission(authorize = false)
    @PostMapping("getMyMenuList")
    public JsonData getMyMenuList() {
        return jsonData(service.getMyMenuList());
    }

    @Description("获取我的权限")
    @Permission(authorize = false)
    @PostMapping("getMyPermissionList")
    public JsonData getMyPermissionList() {
        return jsonData(service.getMyPermissionList());
    }

    @Description("修改我的密码")
    @Permission(authorize = false)
    @PostMapping("updateMyPassword")
    public Json updateMyPassword(@RequestBody @Validated({UserEntity.WhenUpdateMyPassword.class}) UserVo userVo, Long userId) {
        userVo.setId(userId);
        service.modifyUserPassword(userVo);
        return json("密码修改成功");
    }

    @Description("找回密码")
    @Permission(login = false)
    @PostMapping("resetMyPassword")
    public Json resetMyPassword(@RequestBody @Validated(UserEntity.WhenResetMyPassword.class) UserVo userVo) {
        service.resetMyPassword(userVo);
        return json("密码重置成功");
    }

    @Description("注册账号")
    @Permission(login = false)
    @PostMapping("register")
    public Json register(@RequestBody @Validated({UserEntity.WhenRegister.class}) UserVo userVo) {
        service.register(userVo);
        return json("注册成功");
    }

    @Description("账号密码登录")
    @Permission(login = false)
    @PostMapping("login")
    public JsonData login(@RequestBody @Validated({UserEntity.WhenLogin.class}) UserVo userVo, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_ACCOUNT_PASSWORD, userVo, httpServletResponse);
    }

    @Description("邮箱验证码登录")
    @Permission(login = false)
    @PostMapping("loginViaEmail")
    public JsonData loginViaEmail(@RequestBody @Validated({UserEntity.WhenLoginViaEmail.class}) UserVo userVo, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_EMAIL_CODE, userVo, httpServletResponse);
    }

    /**
     * <h1>处理用户登录</h1>
     *
     * @param userLoginType 登录方式
     * @param userVo        登录数据
     * @param response      响应的请求
     * @return JsonData
     */
    private JsonData doLogin(UserLoginType userLoginType, UserVo userVo, HttpServletResponse response) {
        String accessToken = "";
        switch (userLoginType) {
            case VIA_ACCOUNT_PASSWORD:
                accessToken = service.login(userVo);
                break;
            case VIA_EMAIL_CODE:
                accessToken = service.loginViaEmail(userVo);
                break;
            default:
                Result.ERROR.show("暂不支持的登录方式");
        }

        // 开始处理Oauth2登录逻辑
        Long userId = securityUtil.getUserIdFromAccessToken(accessToken);

        // 存储Cookies
        String cookieString = RandomUtil.randomString(32);
        service.saveCookie(userId, cookieString);
        response.addCookie(CookieUtil.getAuthorizeCookie(cookieString));

        String appKey = userVo.getAppKey();
        if (StrUtil.isAllBlank(appKey)) {
            return jsonData(accessToken, "登录成功,请存储你的访问凭证");
        }

        // 验证应用信息
        AppEntity appEntity = appService.getByAppKey(appKey);
        AppVo appVo = appEntity.copyTo(AppVo.class);
        Result.PARAM_INVALID.whenNull(appEntity, "登录失败,错误的应用ID");

        // 生成临时身份令牌code
        String code = RandomUtil.randomString(32);
        appVo.setCode(code);

        // 缓存临时身份令牌code
        service.saveOauthCode(userId, appVo);
        return jsonData(code, "登录成功,请重定向此Code");
    }
}
