package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.curd.permission.PermissionUtil;
import cn.hamm.spms.SpmsApplication;
import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_DELETE;

/**
 * <h1>权限</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class PermissionService extends BaseService<PermissionEntity, PermissionRepository> {
    /**
     * 按标识查询权限
     *
     * @param identity 权限标识
     * @return 权限，不存在时返回 {@code null}
     */
    public PermissionEntity getPermissionByIdentity(String identity) {
        return repository.getByIdentity(identity);
    }

    @Override
    protected void beforeAppDelete(@NotNull PermissionEntity permission) {
        FORBIDDEN_DELETE.when(permission.getIsSystem(), "系统内置权限无法被删除!");
        TreeUtil.ensureNoChildrenBeforeDelete(permission.getId(), id -> filter(new PermissionEntity().setParentId(id)));
    }

    @Override
    protected @NotNull List<PermissionEntity> afterGetList(@NotNull List<PermissionEntity> list) {
        list.forEach(PermissionEntity::excludeNotMeta);
        return list;
    }

    /**
     * 扫描并同步代码里声明的系统权限
     *
     * @apiNote 按 {@code identity} 做 upsert，启动时调用。
     * 只会新增和更新，代码里删掉的权限不会自动从库里删掉，避免升级时误伤已授权的角色
     */
    public void loadPermission() {
        List<PermissionEntity> permissions = PermissionUtil.scanPermission(SpmsApplication.class.getPackageName(), PermissionEntity.class);
        for (var permission : permissions) {
            PermissionEntity exist = getPermissionByIdentity(permission.getIdentity());
            long existId;
            if (Objects.isNull(exist)) {
                exist = new PermissionEntity()
                        .setName(permission.getName())
                        .setIdentity(permission.getIdentity())
                        .setIsSystem(true);
                existId = add(exist);
            } else {
                existId = exist.getId();
                exist.setName(permission.getName())
                        .setIdentity(permission.getIdentity())
                        .setIsSystem(true);
                updateToDatabase(exist);
            }
            exist = get(existId);
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
                    updateToDatabase(existSub);
                }
            }
        }
    }
}
