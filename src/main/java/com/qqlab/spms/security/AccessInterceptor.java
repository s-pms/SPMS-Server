package com.qqlab.spms.security;

import cn.hamm.airpower.config.GlobalConfig;
import cn.hamm.airpower.request.RequestUtil;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.security.AbstractAccessInterceptor;
import cn.hamm.airpower.security.AccessUtil;
import cn.hamm.airpower.security.SecurityUtil;
import com.qqlab.spms.module.personnel.role.RoleEntity;
import com.qqlab.spms.module.personnel.user.UserEntity;
import com.qqlab.spms.module.personnel.user.UserService;
import com.qqlab.spms.module.system.log.LogEntity;
import com.qqlab.spms.module.system.log.LogService;
import com.qqlab.spms.module.system.permission.PermissionEntity;
import com.qqlab.spms.module.system.permission.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <h1>权限拦截器</h1>
 *
 * @author Hamm
 */
public class AccessInterceptor extends AbstractAccessInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private LogService logService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * <h2>验证指定的用户是否有指定权限标识的权限</h2>
     *
     * @param userId             用户ID
     * @param permissionIdentity 权限标识
     * @return 验证结果
     */
    @Override
    public boolean checkPermissionAccess(Long userId, String permissionIdentity) {
        UserEntity existUser = userService.getById(userId);
        if (existUser.getIsSystem()) {
            return true;
        }
        PermissionEntity needPermission = permissionService.getPermissionByIdentity(permissionIdentity);
        for (RoleEntity role : existUser.getRoleList()) {
            if (role.getIsSystem()) {
                return true;
            }
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
    public void afterCompletion(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object object, Exception ex) {
        HandlerMethod handlerMethod = (HandlerMethod) object;
        //取出控制器和方法
        Class<?> clazz = handlerMethod.getBeanType();
        Method method = handlerMethod.getMethod();
        String accessToken = request.getHeader(GlobalConfig.authorizeHeader);
        Long userId = null;
        if (StringUtils.hasLength(accessToken)) {
            try {
                userId = securityUtil.getUserIdFromAccessToken(accessToken);
            } catch (Exception ignored) {

            }
        }
        Integer appVersion = null;
        try {
            appVersion = request.getIntHeader(GlobalConfig.appVersionHeader);
            if (appVersion <= 0) {
                appVersion = null;
            }
        } catch (Exception ignored) {

        }
        String action = AccessUtil.getPermissionIdentity(clazz, method);
        PermissionEntity permissionEntity = permissionService.getPermissionByIdentity(action);
        if (Objects.nonNull(permissionEntity)) {
            action = permissionEntity.getName();
        }
        LogEntity logEntity = new LogEntity();
        logEntity.setIp(RequestUtil.getIpAddress(request))
                .setAction(action)
                .setVersion(appVersion)
                .setUserId(userId)
        ;
        logService.add(logEntity);
    }
}
