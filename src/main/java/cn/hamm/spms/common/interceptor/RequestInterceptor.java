package cn.hamm.spms.common.interceptor;

import cn.hamm.airpower.config.Configs;
import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.config.MessageConstant;
import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.airpower.interceptor.AbstractRequestInterceptor;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.annotation.DisableLog;
import cn.hamm.spms.common.config.AppConstant;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.system.log.LogEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    final static String LOG_REQUEST_KEY = "logId";

    /**
     * <h2>验证指定的用户是否有指定权限标识的权限</h2>
     *
     * @param userId             用户ID
     * @param permissionIdentity 权限标识
     * @param request            请求对象
     * @apiNote 抛出异常则为拦截
     */
    @Override
    protected void checkUserPermission(Long userId, String permissionIdentity, HttpServletRequest request) {
        UserEntity existUser = Services.getUserService().get(userId);
        if (existUser.isRootUser()) {
            return;
        }
        PermissionEntity needPermission = Services.getPermissionService().getPermissionByIdentity(permissionIdentity);
        if (existUser.getRoleList().stream().flatMap(role -> role.getPermissionList().stream()).anyMatch(permission -> needPermission.getId().equals(permission.getId()))) {
            return;
        }
        ServiceError.FORBIDDEN.show(String.format(
                MessageConstant.ACCESS_DENIED, needPermission.getName(), needPermission.getIdentity()
        ));
    }

    /**
     * <h2>拦截请求</h2>
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param clazz    控制器类
     * @param method   执行方法
     * @apiNote 抛出异常则为拦截
     */
    @Override
    protected void interceptRequest(HttpServletRequest request, HttpServletResponse response, Class<?> clazz, Method method) {
        DisableLog disableLog = Utils.getReflectUtil().getAnnotation(DisableLog.class, method);
        if (Objects.nonNull(disableLog)) {
            return;
        }
        String accessToken = request.getHeader(Configs.getServiceConfig().getAuthorizeHeader());
        Long userId = null;
        int appVersion = request.getIntHeader(AppConstant.APP_VERSION_HEADER);
        String platform = Constant.EMPTY_STRING;
        String action = request.getRequestURI();
        try {
            userId = Utils.getSecurityUtil().getIdFromAccessToken(accessToken);
            platform = request.getHeader(AppConstant.APP_PLATFORM_HEADER);
            String description = Utils.getReflectUtil().getDescription(method);
            if (!description.equals(method.getName())) {
                action = description;
            }
        } catch (Exception ignored) {
        }
        String identity = Utils.getAccessUtil().getPermissionIdentity(clazz, method);
        PermissionEntity permissionEntity = Services.getPermissionService().getPermissionByIdentity(identity);
        if (Objects.nonNull(permissionEntity)) {
            action = permissionEntity.getName();
        }
        long logId = Services.getLogService().add(new LogEntity()
                .setIp(Utils.getRequestUtil().getIpAddress(request))
                .setAction(action)
                .setPlatform(platform)
                .setRequest(getRequestBody(request))
                .setVersion(Math.max(1, appVersion))
                .setUserId(userId)
        );
        setShareData(RequestInterceptor.LOG_REQUEST_KEY, logId);
    }
}
