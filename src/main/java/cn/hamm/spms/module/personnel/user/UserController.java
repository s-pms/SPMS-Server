package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.helper.CookieHelper;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.AccessTokenUtil;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;


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
        user.setRoleList(null);
        service.update(user);
        return Json.success("资料修改成功");
    }

    @Description("发送邮件")
    @Permission(login = false)
    @PostMapping("sendMail")
    public Json sendMail(@RequestBody @Validated(WhenSendEmail.class) UserEntity user) throws MessagingException {
        service.sendMail(user.getEmail());
        return Json.success("发送成功");
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

    @Description("修改我的密码")
    @Permission(authorize = false)
    @PostMapping("updateMyPassword")
    public Json updateMyPassword(@RequestBody @Validated(WhenUpdateMyPassword.class) UserEntity user) {
        user.setId(getCurrentUserId());
        service.modifyUserPassword(user);
        return Json.success("密码修改成功");
    }

    @Description("账号密码登录")
    @Permission(login = false)
    @PostMapping("login")
    public Json login(@RequestBody @Validated(WhenLogin.class) UserEntity user, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_ACCOUNT_PASSWORD, user, httpServletResponse);
    }

    @Description("邮箱验证码登录")
    @Permission(login = false)
    @PostMapping("loginViaEmail")
    public Json loginViaEmail(@RequestBody @Validated(WhenLoginViaEmail.class) UserEntity user, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_EMAIL_CODE, user, httpServletResponse);
    }

    @Description("手机验证码登录")
    @Permission(login = false)
    @PostMapping("loginViaPhone")
    public Json loginViaPhone(@RequestBody @Validated(WhenLoginViaPhone.class) UserEntity user, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_PHONE_CODE, user, httpServletResponse);
    }

    /**
     * <h1>处理用户登录</h1>
     *
     * @param userLoginType 登录方式
     * @param user          登录数据
     * @param response      响应的请求
     * @return JsonData
     */
    private Json doLogin(@NotNull UserLoginType userLoginType, UserEntity user, HttpServletResponse response) {
        String accessToken = switch (userLoginType) {
            case VIA_ACCOUNT_PASSWORD -> service.login(user);
            case VIA_EMAIL_CODE -> service.loginViaEmail(user);
            case VIA_PHONE_CODE -> service.loginViaPhone(user);
        };

        // 开始处理Oauth2登录逻辑
        Long userId = AccessTokenUtil.create().getPayloadId(accessToken, serviceConfig.getAccessTokenSecret());

        // 存储Cookies
        String cookieString = RandomUtil.randomString();
        service.saveCookie(userId, cookieString);
        response.addCookie(cookieHelper.getAuthorizeCookie(cookieString));

        return Json.data(accessToken, "登录成功,请存储你的访问凭证");
    }
}
