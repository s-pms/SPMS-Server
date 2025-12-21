package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.cookie.CookieHelper;
import cn.hamm.airpower.curd.Curd;
import cn.hamm.airpower.meta.ExposeAll;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginEntity;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginService;
import cn.hamm.spms.module.personnel.user.department.UserDepartmentService;
import cn.hamm.spms.module.personnel.user.enums.UserLoginType;
import cn.hamm.spms.module.personnel.user.role.UserRoleService;
import cn.hamm.spms.module.personnel.user.token.PersonalTokenEntity;
import cn.hamm.spms.module.personnel.user.token.PersonalTokenService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_DISABLED;
import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_EDIT;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("user")
@Description("用户")
@Extends({Curd.Disable, Curd.Enable})
public class UserController extends BaseController<UserEntity, UserService, UserRepository> implements IUserAction {
    @Autowired
    private CookieHelper cookieHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserThirdLoginService userThirdLoginService;

    @Autowired
    private PersonalTokenService personalTokenService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserDepartmentService userDepartmentService;

    @Description("获取我的信息")
    @Permission(authorize = false)
    @PostMapping("getMyInfo")
    @ExposeAll(UserEntity.class)
    public Json getMyInfo() {
        return Json.data(service.get(getCurrentUserId()));
    }

    @Description("修改我的信息")
    @Permission(authorize = false)
    @PostMapping("updateMyInfo")
    public Json updateMyInfo(@RequestBody @Validated(WhenUpdateMyInfo.class) UserEntity user) {
        user.setId(getCurrentUserId());
        user.setPhone(null).setEmail(null).setRealName(null).setIdCard(null);
        service.update(user);
        return Json.success("资料修改成功");
    }

    @Description("获取我的私人令牌列表")
    @Permission(authorize = false)
    @PostMapping("getMyPersonalTokenList")
    @ExposeAll(PersonalTokenEntity.class)
    public Json getMyPersonalTokenList() {
        UserEntity user = new UserEntity().setId(getCurrentUserId());
        List<PersonalTokenEntity> list = personalTokenService.filter(new PersonalTokenEntity().setUser(user));
        return Json.data(list);
    }

    @Description("禁用我的令牌")
    @Permission(authorize = false)
    @PostMapping("disableMyPersonalToken")
    public Json disableMyPersonalToken(@RequestBody @Validated(WhenIdRequired.class) PersonalTokenEntity personalToken) {
        PersonalTokenEntity exist = personalTokenService.get(personalToken.getId());
        FORBIDDEN_EDIT.whenNotEquals(exist.getUser().getId(), getCurrentUserId(), "你没有权限禁用此令牌");
        personalTokenService.disable(personalToken.getId());
        return Json.success("禁用成功");
    }

    @Override
    protected UserEntity afterGetDetail(@NotNull UserEntity user) {
        return user.setDepartmentList(userDepartmentService.getDepartmentList(user.getId()))
                .setRoleList(userRoleService.getRoleList(user.getId()));
    }

    @Description("启用我的令牌")
    @Permission(authorize = false)
    @PostMapping("enableMyPersonalToken")
    public Json enableMyPersonalToken(@RequestBody @Validated(WhenIdRequired.class) PersonalTokenEntity personalToken) {
        PersonalTokenEntity exist = personalTokenService.get(personalToken.getId());
        FORBIDDEN_EDIT.whenNotEquals(exist.getUser().getId(), getCurrentUserId(), "你没有权限启用此令牌");
        personalTokenService.enable(personalToken.getId());
        return Json.success("启用成功");
    }

    @Description("删除我的令牌")
    @Permission(authorize = false)
    @PostMapping("deleteMyPersonalToken")
    public Json deleteMyPersonalToken(@RequestBody @Validated(WhenIdRequired.class) PersonalTokenEntity personal) {
        PersonalTokenEntity exist = personalTokenService.get(personal.getId());
        FORBIDDEN_EDIT.whenNotEquals(exist.getUser().getId(), getCurrentUserId(), "你没有权限删除此令牌");
        personalTokenService.delete(personal.getId());
        return Json.success("删除成功");
    }

    @Description("创建我的令牌")
    @Permission(authorize = false)
    @PostMapping("createMyPersonalToken")
    public Json createMyPersonalToken(@RequestBody @Validated(WhenAdd.class) PersonalTokenEntity personal) {
        long id = personalTokenService.add(personal.setUser(new UserEntity().setId(getCurrentUserId())));
        PersonalTokenEntity personalToken = personalTokenService.get(id);
        String token = personalToken.getToken();
        return Json.data(token, "创建成功");
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
        List<String> permissions = permissionList.stream()
                .map(PermissionEntity::getIdentity)
                .collect(Collectors.toList());
        return Json.data(permissions);
    }

    @Description("获取我绑定的社交账号")
    @Permission(authorize = false)
    @PostMapping("getMyThirdList")
    public Json getMyThirdList() {
        UserEntity user = service.get(getCurrentUserId());
        List<UserThirdLoginEntity> list = userThirdLoginService.filter(new UserThirdLoginEntity().setUser(user));
        return Json.data(list.stream()
                .map(item -> item.setUser(null))
        );
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
        cookie.setPath(CookieHelper.DEFAULT_PATH);
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
     * @param user          登录数据
     * @param response      响应的请求
     * @return JsonData
     */
    private Json doLogin(@NotNull UserLoginType userLoginType, UserEntity user, HttpServletResponse response) {
        UserEntity exist = switch (userLoginType) {
            case VIA_ACCOUNT_PASSWORD -> service.login(user);
            case VIA_EMAIL_CODE -> service.loginViaEmail(user);
        };
        FORBIDDEN_DISABLED.when(exist.getIsDisabled(), "登录失败，你的账号已被禁用");
        return Json.data(userService.loginWithCookieAndResponse(response, exist), "登录成功");
    }
}
