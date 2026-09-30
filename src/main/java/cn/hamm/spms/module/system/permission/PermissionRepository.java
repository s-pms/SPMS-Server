package cn.hamm.spms.module.system.permission;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>权限</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PermissionRepository extends BaseRepository<PermissionEntity> {
    /**
     * 按权限标识查询
     *
     * @param identity 权限标识
     * @return 权限，不存在时返回 {@code null}
     */
    PermissionEntity getByIdentity(String identity);
}
