package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
    @Description("获取我的信息")
    @Permission(authorize = false)
    @RequestMapping("getMyInfo")
    @Filter(WhenGetMyInfo.class)
    public Json getMyInfo(Long userId) {
        return Json.data(service.get(userId));
    }

    @Description("修改我的信息")
    @Permission(authorize = false)
    @RequestMapping("updateMyInfo")
    public Json updateMyInfo(@RequestBody @Validated(WhenUpdateMyInfo.class) UserEntity userEntity, Long userId) {
        userEntity.setId(userId);
        userEntity.setRoleList(null);
        service.update(userEntity);
        return Json.success("资料修改成功");
    }

    @Description("获取我的菜单")
    @Permission(authorize = false)
    @RequestMapping("getMyMenuList")
    public Json getMyMenuList() {
        return Json.data(service.getMenuListByUserId(getCurrentUserId()));
    }

    @Description("获取我的权限")
    @Permission(authorize = false)
    @RequestMapping("getMyPermissionList")
    public Json getMyPermissionList() {
        List<PermissionEntity> permissionList = service.getPermissionListByUserId(getCurrentUserId());
        List<String> permissions = new ArrayList<>();
        permissionList.forEach(permission -> permissions.add(permission.getIdentity()));
        return Json.data(permissions);
    }

    @Description("修改我的密码")
    @Permission(authorize = false)
    @RequestMapping("updateMyPassword")
    public Json updateMyPassword(@RequestBody @Validated(WhenUpdateMyPassword.class) UserEntity userEntity, Long userId) {
        userEntity.setId(userId);
        service.modifyUserPassword(userEntity);
        return Json.success("密码修改成功");
    }

    @Description("账号密码登录")
    @Permission(login = false)
    @RequestMapping("login")
    public Json login(@RequestBody @Validated(WhenLogin.class) UserEntity userEntity, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_ACCOUNT_PASSWORD, userEntity, httpServletResponse);
    }

    @Description("邮箱验证码登录")
    @Permission(login = false)
    @RequestMapping("loginViaEmail")
    public Json loginViaEmail(@RequestBody @Validated(WhenLoginViaEmail.class) UserEntity userEntity, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_EMAIL_CODE, userEntity, httpServletResponse);
    }

    @Description("手机验证码登录")
    @Permission(login = false)
    @RequestMapping("loginViaPhone")
    public Json loginViaPhone(@RequestBody @Validated(WhenLoginViaPhone.class) UserEntity userEntity, HttpServletResponse httpServletResponse) {
        return doLogin(UserLoginType.VIA_PHONE_CODE, userEntity, httpServletResponse);
    }

    /**
     * <h1>处理用户登录</h1>
     *
     * @param userLoginType 登录方式
     * @param userEntity    登录数据
     * @param response      响应的请求
     * @return JsonData
     */
    private Json doLogin(@NotNull UserLoginType userLoginType, UserEntity userEntity, HttpServletResponse response) {
        String accessToken = switch (userLoginType) {
            case VIA_ACCOUNT_PASSWORD -> service.login(userEntity);
            case VIA_EMAIL_CODE -> service.loginViaEmail(userEntity);
            case VIA_PHONE_CODE -> service.loginViaPhone(userEntity);
        };

        // 开始处理Oauth2登录逻辑
        Long userId = Utils.getSecurityUtil().getIdFromAccessToken(accessToken);

        // 存储Cookies
        RandomUtil randomUtil = Utils.getRandomUtil();
        String cookieString = randomUtil.randomString();
        service.saveCookie(userId, cookieString);
        response.addCookie(Utils.getCookieUtil().getAuthorizeCookie(cookieString));

        String appKey = userEntity.getAppKey();
        if (!StringUtils.hasText(appKey)) {
            return Json.data(accessToken, "登录成功,请存储你的访问凭证");
        }

        // 验证应用信息
        OpenAppEntity openApp = Services.getOpenAppService().getByAppKey(appKey);
        ServiceError.PARAM_INVALID.whenNull(openApp, "登录失败,错误的应用ID");

        // 生成临时身份令牌code
        String code = randomUtil.randomString(32);
        openApp.setCode(code);

        // 缓存临时身份令牌code
        service.saveOauthCode(userId, openApp);
        return Json.data(code, "登录成功,请重定向此Code");
    }
}
