package cn.hamm.spms.module.personnel.role.permission;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class RolePermissionService extends BaseService<RolePermissionEntity, RolePermissionRepository> {
    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    public List<PermissionEntity> getPermissionList(long roleId) {
        RolePermissionEntity rolePermissionFilter = new RolePermissionEntity().setRole(
                new RoleEntity().setId(roleId)
        );
        return filter(rolePermissionFilter)
                .stream()
                .map(RolePermissionEntity::getPermission)
                .toList();
    }
}
