package cn.hamm.spms.common.interceptor;

import cn.hamm.airpower.interceptor.AbstractRequestInterceptor;
import cn.hamm.airpower.util.AccessTokenUtil;
import cn.hamm.airpower.util.PermissionUtil;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hamm.airpower.util.RequestUtil;
import cn.hamm.spms.common.annotation.DisableLog;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.system.log.LogEntity;
import cn.hamm.spms.module.system.log.LogService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import cn.hamm.spms.module.system.permission.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

import static cn.hamm.airpower.config.Constant.STRING_EMPTY;
import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;
import static cn.hamm.spms.common.config.AppConstant.APP_PLATFORM_HEADER;
import static cn.hamm.spms.common.config.AppConstant.APP_VERSION_HEADER;

/**
 * <h1>请求拦截器</h1>
 *
 * @author Hamm.cn
 */
@Component
public class RequestInterceptor extends AbstractRequestInterceptor {
    /**
     * <h3>日志前缀</h3>
     */
    final static String LOG_REQUEST_KEY = "logId";

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    /**
     * <h3>验证指定的用户是否有指定权限标识的权限</h3>
     *
     * @param userId             用户ID
     * @param permissionIdentity 权限标识
     * @param request            请求对象
     * @apiNote 抛出异常则为拦截
     */
    @Override
    protected void checkUserPermission(long userId, String permissionIdentity, HttpServletRequest request) {
        UserEntity existUser = userService.get(userId);
        if (existUser.isRootUser()) {
            return;
        }
        PermissionEntity needPermission = permissionService.getPermissionByIdentity(permissionIdentity);
        if (existUser.getRoleList().stream()
                .flatMap(role -> role.getPermissionList().stream())
                .anyMatch(permission -> needPermission.getId().equals(permission.getId()))
        ) {
            return;
        }
        FORBIDDEN.show(String.format(
                "你无权访问 %s (%s)", needPermission.getName(), needPermission.getIdentity()
        ));
    }

    /**
     * <h3>拦截请求</h3>
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param clazz    控制器类
     * @param method   执行方法
     * @apiNote 抛出异常则为拦截
     */
    @Override
    protected void interceptRequest(HttpServletRequest request, HttpServletResponse response, Class<?> clazz, Method method) {
        DisableLog disableLog = ReflectUtil.getAnnotation(DisableLog.class, method);
        if (Objects.nonNull(disableLog)) {
            return;
        }
        String accessToken = request.getHeader(serviceConfig.getAuthorizeHeader());
        Long userId = null;
        int appVersion = request.getIntHeader(APP_VERSION_HEADER);
        String platform = STRING_EMPTY;
        String action = request.getRequestURI();
        try {
            userId = AccessTokenUtil.create().getPayloadId(accessToken, serviceConfig.getAccessTokenSecret());
            platform = request.getHeader(APP_PLATFORM_HEADER);
            String description = ReflectUtil.getDescription(method);
            if (!description.equals(method.getName())) {
                action = description;
            }
        } catch (Exception ignored) {
        }
        String identity = PermissionUtil.getPermissionIdentity(clazz, method);
        PermissionEntity permissionEntity = permissionService.getPermissionByIdentity(identity);
        if (Objects.nonNull(permissionEntity)) {
            action = permissionEntity.getName();
        }
        long logId = logService.add(new LogEntity()
                .setIp(RequestUtil.getIpAddress(request))
                .setAction(action)
                .setPlatform(platform)
                .setRequest(getRequestBody(request))
                .setVersion(Math.max(1, appVersion))
                .setUserId(userId)
        );
        setShareData(RequestInterceptor.LOG_REQUEST_KEY, logId);
    }
}
