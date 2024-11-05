package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.root.delegate.TreeServiceDelegate;
import cn.hamm.airpower.util.PermissionUtil;
import cn.hamm.spms.Application;
import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class PermissionService extends BaseService<PermissionEntity, PermissionRepository> {
    /**
     * <h2>通过标识获取一个权限</h2>
     *
     * @param identity 权限标识
     * @return 权限
     */
    public PermissionEntity getPermissionByIdentity(String identity) {
        return repository.getByIdentity(identity);
    }

    @Override
    protected void beforeDelete(long id) {
        PermissionEntity permission = get(id);
        ServiceError.FORBIDDEN_DELETE.when(permission.getIsSystem(), "系统内置权限无法被删除!");
        TreeServiceDelegate.ensureNoChildrenBeforeDelete(this, id);
    }

    @Override
    protected @NotNull List<PermissionEntity> afterGetList(@NotNull List<PermissionEntity> list) {
        list.forEach(RootEntity::excludeBaseData);
        return list;
    }

    public void loadPermission() {
        List<PermissionEntity> permissions = PermissionUtil.scanPermission(Application.class.getPackageName(), PermissionEntity.class);
        for (var permission : permissions) {
            PermissionEntity exist = getPermissionByIdentity(permission.getIdentity());
            if (Objects.isNull(exist)) {
                exist = new PermissionEntity()
                        .setName(permission.getName())
                        .setIdentity(permission.getIdentity())
                        .setIsSystem(true);
                exist.setId(add(exist));
            } else {
                exist.setName(permission.getName())
                        .setIdentity(permission.getIdentity())
                        .setIsSystem(true);
                update(exist);
            }
            exist = get(exist.getId());
            for (PermissionEntity subPermission : permission.getChildren()) {
                PermissionEntity existSub = getPermissionByIdentity(subPermission.getIdentity());
                if (Objects.isNull(existSub)) {
                    existSub = new PermissionEntity()
                            .setName(subPermission.getName())
                            .setIdentity(subPermission.getIdentity())
                            .setIsSystem(true)
                            .setParentId(exist.getId());
                    add(existSub);
                } else {
                    existSub.setName(subPermission.getName())
                            .setIdentity(subPermission.getIdentity())
                            .setIsSystem(true)
                            .setParentId(exist.getId());
                    update(existSub);
                }
            }
        }
    }
}
