package cn.hamm.starter.module.system.permission;

import cn.hamm.airpower.root.RootRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface PermissionRepository extends RootRepository<PermissionEntity> {
    /**
     * <h1>根据权限标识获取一个权限</h1>
     *
     * @param identity 权限标识
     * @return 权限实体
     */
    PermissionEntity getByIdentity(String identity);
}
