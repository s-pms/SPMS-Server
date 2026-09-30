package cn.hamm.spms.common.interceptor;

import cn.hamm.airpower.core.AccessTokenUtil;
import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.curd.interceptor.CurdRequestInterceptor;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.personnel.user.enums.UserTokenType;
import cn.hamm.spms.module.personnel.user.token.PersonalTokenEntity;
import cn.hamm.spms.module.personnel.user.token.PersonalTokenService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import cn.hamm.spms.module.system.permission.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.airpower.exception.Errors.UNAUTHORIZED;

/**
 * <h1>登录与权限拦截</h1>
 *
 * @author Hamm.cn
 */
@Component
@Slf4j
public class RequestInterceptor extends CurdRequestInterceptor {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private PersonalTokenService personalTokenService;

    /**
     * 验证指定的用户是否有指定权限标识的权限
     *
     * @param verifiedToken      合法令牌
     * @param permissionIdentity 权限标识
     * @param request            请求对象
     * @apiNote 抛出异常则为拦截
     */
    @Override
    public void checkUserPermission(
            AccessTokenUtil.@NotNull VerifiedToken verifiedToken,
            String permissionIdentity,
            HttpServletRequest request
    ) {
        long userId = verifiedToken.getPayloadId();
        UserEntity currentUser = userService.getWithEnable(userId);
        if (currentUser.isRootUser()) {
            return;
        }
        PermissionEntity needPermission = permissionService.getPermissionByIdentity(permissionIdentity);
        if (Objects.isNull(needPermission)) {
            log.warn("权限标识在权限表中不存在，请检查权限是否已对账: {}", permissionIdentity);
            FORBIDDEN.show("接口权限配置缺失，请联系管理员: " + permissionIdentity);
        }
        Set<PermissionEntity> ownedPermissions = currentUser.getRoleList().stream()
                .map(RoleEntity::getPermissionList)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        if (ownedPermissions.stream()
                .anyMatch(permission -> needPermission.getId().equals(permission.getId()))
        ) {
            return;
        }
        FORBIDDEN.show(String.format(
                "你无权访问 %s (%s)", needPermission.getName(), needPermission.getIdentity()
        ));
    }

    /**
     * 校验访问令牌
     *
     * @param accessToken 访问令牌
     * @return 校验通过的令牌
     * @apiNote 在父类校验签名之外追加三件事：令牌类型必须在 {@code UserTokenType} 枚举内、
     * 个人令牌未被禁用、用户处于启用状态。缺任何一项都直接抛异常拦截
     */
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
        long userId = verifiedToken.getPayloadId();
        userService.getWithEnable(userId);
        return verifiedToken;
    }
}
