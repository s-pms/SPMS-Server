package cn.hamm.spms.common.interceptor;

import cn.hamm.airpower.config.AirConfig;
import cn.hamm.airpower.enums.Result;
import cn.hamm.airpower.interceptor.AbstractRequestInterceptor;
import cn.hamm.airpower.util.AirUtil;
import cn.hamm.spms.common.annotation.LogDisabled;
import cn.hamm.spms.common.config.Constant;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.system.log.LogEntity;
import cn.hamm.spms.module.system.log.LogService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import cn.hamm.spms.module.system.permission.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <h1>请求拦截器</h1>
 *
 * @author Hamm.cn
 */
@Component
public class RequestInterceptor extends AbstractRequestInterceptor {
    /**
     * <h2>日志前缀</h2>
     */
    public final static String LOG_REQUEST_KEY = "logId";

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private LogService logService;

    @Override
    public boolean checkPermissionAccess(Long userId, String permissionIdentity, HttpServletRequest request) {
        UserEntity existUser = userService.get(userId);
        if (existUser.isRootUser()) {
            return true;
        }
        PermissionEntity needPermission = permissionService.getPermissionByIdentity(permissionIdentity);
        for (RoleEntity role : existUser.getRoleList()) {
            for (PermissionEntity permission : role.getPermissionList()) {
                if (needPermission.getId().equals(permission.getId())) {
                    return true;
                }
            }
        }
        Result.FORBIDDEN.show("你无权访问 " + needPermission.getName() + "(" + needPermission.getIdentity() + ")");
        return false;
    }

    @Override
    protected void beforeHandleRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            Class<?> clazz,
            @NotNull Method method
    ) {
        LogDisabled logDisabled = AirUtil.getReflectUtil().getAnnotation(LogDisabled.class, method);
        if (Objects.nonNull(logDisabled)) {
            return;
        }
        String accessToken = request.getHeader(AirConfig.getGlobalConfig().getAuthorizeHeader());
        Long userId = null;
        int appVersion = request.getIntHeader(Constant.APP_VERSION_HEADER);
        String platform = "";
        String action = request.getRequestURI();
        try {
            userId = AirUtil.getSecurityUtil().getUserIdFromAccessToken(accessToken);
            platform = request.getHeader(Constant.APP_PLATFORM_HEADER);
            String description = AirUtil.getReflectUtil().getDescription(method);
            if (!description.equals(method.getName())) {
                action = description;
            }
        } catch (Exception ignored) {
        }
        String identity = AirUtil.getAccessUtil().getPermissionIdentity(clazz, method);
        PermissionEntity permissionEntity = permissionService.getPermissionByIdentity(identity);
        if (Objects.nonNull(permissionEntity)) {
            action = permissionEntity.getName();
        }
        long logId = logService.add(new LogEntity()
                .setIp(AirUtil.getRequestUtil().getIpAddress(request))
                .setAction(action)
                .setPlatform(platform)
                .setRequest(getRequestBody(request))
                .setVersion(Math.max(1, appVersion))
                .setUserId(userId)
        );
        setShareData(RequestInterceptor.LOG_REQUEST_KEY, logId);
    }
}
