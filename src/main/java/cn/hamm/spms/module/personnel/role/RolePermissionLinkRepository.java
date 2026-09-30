package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <h1>角色权限关联数据源</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface RolePermissionLinkRepository extends BaseRepository<RolePermissionLinkEntity> {

    /**
     * 按角色 ID 查关联记录
     *
     * @param roleId 角色 ID
     * @return 关联记录
     */
    @Query("select l from RolePermissionLinkEntity l where l.role.id = :roleId")
    List<RolePermissionLinkEntity> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 按一批角色 ID 查关联记录（一次查询，避免逐个查造成 N+1）
     *
     * @param roleIds 角色 ID 集合
     * @return 关联记录
     */
    @Query("select l from RolePermissionLinkEntity l where l.role.id in :roleIds")
    List<RolePermissionLinkEntity> findByRoleIds(@Param("roleIds") List<Long> roleIds);
}
