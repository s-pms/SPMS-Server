package cn.hamm.spms.common.interceptor;

import cn.hamm.airpower.access.AccessTokenUtil;
import cn.hamm.airpower.dictionary.DictionaryUtil;
import cn.hamm.airpower.interceptor.AbstractRequestInterceptor;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.role.permission.RolePermissionService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.personnel.user.enums.UserTokenType;
import cn.hamm.spms.module.personnel.user.role.UserRoleService;
import cn.hamm.spms.module.personnel.user.token.PersonalTokenEntity;
import cn.hamm.spms.module.personnel.user.token.PersonalTokenService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import cn.hamm.spms.module.system.permission.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;
import static cn.hamm.airpower.exception.ServiceError.UNAUTHORIZED;

/**
 * <h1>请求拦截器</h1>
 *
 * @author Hamm.cn
 */
@Component
public class RequestInterceptor extends AbstractRequestInterceptor {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private PersonalTokenService personalTokenService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    /**
     * 验证指定的用户是否有指定权限标识的权限
     *
     * @param verifiedToken      合法令牌
     * @param permissionIdentity 权限标识
     * @param request            请求对象
     * @apiNote 抛出异常则为拦截
     */
    @Override
    public void checkUserPermission(AccessTokenUtil.@NotNull VerifiedToken verifiedToken, String permissionIdentity, HttpServletRequest request) {
        long userId = verifiedToken.getPayloadId();
        UserEntity existUser = userService.getWithEnable(userId);
        if (existUser.isRootUser()) {
            return;
        }
        PermissionEntity needPermission = permissionService.getPermissionByIdentity(permissionIdentity);
        List<RoleEntity> roleList = userRoleService.getRoleList(userId);
        if (roleList.stream()
                .flatMap(role -> rolePermissionService.getPermissionList(role.getId()).stream())
                .anyMatch(permission -> needPermission.getId().equals(permission.getId()))
        ) {
            return;
        }
        FORBIDDEN.show(String.format(
                "你无权访问 %s (%s)", needPermission.getName(), needPermission.getIdentity()
        ));
    }

    @Override
    public AccessTokenUtil.VerifiedToken getVerifiedToken(String accessToken) {
        AccessTokenUtil.VerifiedToken verifiedToken = super.getVerifiedToken(accessToken);
        Object tokenType = verifiedToken.getPayload(UserTokenType.TYPE);
        FORBIDDEN.whenNull(tokenType, "无效的令牌类型");
        UserTokenType userTokenType = DictionaryUtil.getDictionary(UserTokenType.class, Integer.parseInt(tokenType.toString()));
        switch (userTokenType) {
            case PERSONAL:
                PersonalTokenEntity personalToken = personalTokenService.getByToken(accessToken);
                UNAUTHORIZED.whenNull(personalToken, "无效的私人令牌");
                FORBIDDEN.when(personalToken.getIsDisabled(), "私人令牌已被禁用");
                break;
            case OAUTH2:
            case NORMAL:
                break;
            default:
                FORBIDDEN.show("不支持的令牌类型");
        }
        return verifiedToken;
    }
}
