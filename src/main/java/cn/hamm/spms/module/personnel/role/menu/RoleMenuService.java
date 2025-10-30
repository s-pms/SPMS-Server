package cn.hamm.spms.module.personnel.role.menu;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.system.menu.MenuEntity;
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
public class RoleMenuService extends BaseService<RoleMenuEntity, RoleMenuRepository> {
    /**
     * 获取角色的菜单列表
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    public List<MenuEntity> getRoleMenuList(long roleId) {
        RoleMenuEntity roleMenuFilter = new RoleMenuEntity().setRole(
                new RoleEntity().setId(roleId)
        );
        return filter(roleMenuFilter)
                .stream()
                .map(RoleMenuEntity::getMenu)
                .toList();
    }
}
