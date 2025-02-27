package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.helper.CookieHelper;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginEntity;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginService;
import cn.hamm.spms.module.personnel.user.enums.UserLoginType;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_DISABLED;


/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("user")
@Description("用户")
public class UserController extends BaseController<UserEntity, UserService, UserRepository> implements IUserAction {
    @Autowired
    private CookieHelper cookieHelper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserThirdLoginService userThirdLoginService;

    @Description("获取我的信息")
    @Permission(authorize = false)
    @PostMapping("getMyInfo")
    @Filter(WhenGetMyInfo.class)
    public Json getMyInfo() {
        return Json.data(service.get(getCurrentUserId()));
    }

    @Description("修改我的信息")
    @Permission(authorize = false)
    @PostMapping("updateMyInfo")
    public Json updateMyInfo(@RequestBody @Validated(WhenUpdateMyInfo.class) UserEntity user) {
        user.setId(getCurrentUserId());
        user.setRoleList(null).setPhone(null).setEmail(null).setRealName(null).setIdCard(null);
        service.update(user);
        return Json.success("资料修改成功");
    }

    @Description("获取我的菜单")
    @Permission(authorize = false)
    @PostMapping("getMyMenuList")
    public Json getMyMenuList() {
        return Json.data(service.getMenuListByUserId(getCurrentUserId()));
    }

    @Description("获取我的权限")
    @Permission(authorize = false)
    @PostMapping("getMyPermissionList")
    public Json getMyPermissionList() {
        List<PermissionEntity> permissionList = service.getPermissionListByUserId(getCurrentUserId());
        List<String> permissions = new ArrayList<>();
        permissionList.forEach(permission -> permissions.add(permission.getIdentity()));
        return Json.data(permissions);
    }

    @Description("获取我绑定的社交账号")
    @Permission(authorize = false)
    @PostMapping("getMyThirdList")
    public Json getMyThirdList() {
        UserEntity user = service.get(getCurrentUserId());
        List<UserThirdLoginEntity> list = userThirdLoginService.filter(new UserThirdLoginEntity().setUser(user));
        return Json.data(list.stream().map(item -> item.setUser(null)));
    }

    @Description("修改我的密码")
    @Permission(authorize = false)
    @PostMapping("updateMyPassword")
    public Json updateMyPassword(@RequestBody @Validated(WhenUpdateMyPassword.class) UserEntity user) {
        user.setId(getCurrentUserId());
        service.modifyMyPassword(user);
        return Json.success("密码修改成功");
    }

    @Description("找回密码")
    @Permission(login = false)
    @PostMapping("resetMyPassword")
    public Json resetMyPassword(@RequestBody @Validated(WhenResetMyPassword.class) UserEntity user) {
        service.resetMyPassword(user);
        return Json.success("密码重置成功");
    }

    @Description("账号密码登录")
    @Permission(login = false)
    @PostMapping("login")
    public Json login(@RequestBody @Validated(WhenLogin.class) UserEntity user, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_ACCOUNT_PASSWORD, user, httpServletResponse);
    }


    @Description("退出登录")
    @Permission(login = false)
    @PostMapping("logout")
    public Json logout(HttpServletResponse httpServletResponse) {
        Cookie cookie = cookieHelper.getAuthorizeCookie("");
        cookie.setHttpOnly(false);
        cookie.setPath(Constant.SLASH);
        // 清除cookie
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);
        return Json.success("退出登录成功");
    }

    @Description("邮箱验证码登录")
    @Permission(login = false)
    @PostMapping("loginViaEmail")
    public Json loginViaEmail(@RequestBody @Validated(WhenLoginViaEmail.class) UserEntity user, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_EMAIL_CODE, user, httpServletResponse);
    }

    @Description("发送邮件")
    @Permission(login = false)
    @PostMapping("sendEmail")
    public Json sendEmail(@RequestBody @Validated(WhenSendEmail.class) UserEntity user) throws MessagingException {
        service.sendMail(user.getEmail());
        return Json.success("发送成功");
    }

    @Description("发送短信")
    @Permission(login = false)
    @PostMapping("sendSms")
    public Json sendSms(@RequestBody @Validated(WhenSendSms.class) UserEntity user) {
        service.sendSms(user.getPhone());
        return Json.success("发送成功");
    }

    /**
     * <h1>处理用户登录</h1>
     *
     * @param userLoginType 登录方式
     * @param login         登录数据
     * @param response      响应的请求
     * @return JsonData
     */
    private Json doLogin(@NotNull UserLoginType userLoginType, UserEntity login, HttpServletResponse response) {
        UserEntity user = switch (userLoginType) {
            case VIA_ACCOUNT_PASSWORD -> service.login(login);
            case VIA_EMAIL_CODE -> service.loginViaEmail(login);
        };
        FORBIDDEN_DISABLED.when(user.getIsDisabled(), "登录失败，你的账号已被禁用");

        return Json.data(userService.loginWithCookieAndResponse(response, user), "登录成功");
    }
}
