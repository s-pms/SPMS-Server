package cn.hamm.spms.module.personnel.role;

/**
 * <h1>角色的校验分组</h1>
 *
 * @author Hamm.cn
 */
public interface IRoleAction {
    /**
     * 授权菜单时的校验分组
     */
    interface WhenAuthorizeMenu {
    }

    /**
     * 授权权限时的校验分组
     */
    interface WhenAuthorizePermission {
    }
}
