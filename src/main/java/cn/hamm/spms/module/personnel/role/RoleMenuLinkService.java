package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.menu.MenuEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h1>角色菜单关联服务</h1>
 * <p>
 * 承载 {@code role <-> menu} 的多对多关系，取代 {@code @ManyToMany}。
 * </p>
 *
 * @author Hamm.cn
 */
@Service
public class RoleMenuLinkService extends BaseService<RoleMenuLinkEntity, RoleMenuLinkRepository> {

    /**
     * 查出每个角色已授权的菜单
     *
     * @param roleIds 角色 ID 集合
     * @return 角色 ID -> 菜单集合
     */
    public @NotNull Map<Long, Set<MenuEntity>> mapMenusByRoleIds(@NotNull Collection<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return repository.findByRoleIds(new ArrayList<>(roleIds)).stream()
                .collect(Collectors.groupingBy(
                        l -> l.getRole().getId(),
                        Collectors.mapping(
                                RoleMenuLinkEntity::getMenu,
                                Collectors.toCollection(LinkedHashSet::new))));
    }

    /**
     * 同步某个角色的授权（先删后建）
     *
     * @param roleId 角色 ID
     * @param menus  授权的菜单
     */
    public void syncByRoleId(long roleId, @NotNull Collection<MenuEntity> menus) {
        List<RoleMenuLinkEntity> exists = repository.findByRoleId(roleId);
        if (!exists.isEmpty()) {
            repository.deleteAll(exists);
            repository.flush();
        }
        if (menus.isEmpty()) {
            return;
        }
        RoleEntity role = new RoleEntity().setId(roleId);
        for (MenuEntity menu : menus) {
            if (Objects.nonNull(menu) && Objects.nonNull(menu.getId())) {
                addAndGet(new RoleMenuLinkEntity().setRole(role).setMenu(menu));
            }
        }
    }

    /**
     * 收集一组角色的全部菜单（去重）
     *
     * @param roles 角色集合
     * @return 菜单集合
     */
    public @NotNull Set<MenuEntity> collectMenus(@NotNull Collection<RoleEntity> roles) {
        return roles.stream()
                .map(RoleEntity::getMenuList)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
