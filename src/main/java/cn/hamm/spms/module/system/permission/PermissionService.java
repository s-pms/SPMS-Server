package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.web.access.PermissionUtil;
import cn.hamm.airpower.web.mcp.McpService;
import cn.hamm.airpower.web.mcp.model.McpTool;
import cn.hamm.spms.Application;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.permission.enums.PermissionType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN_DELETE;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class PermissionService extends BaseService<PermissionEntity, PermissionRepository> {
    /**
     * 通过标识获取一个权限
     *
     * @param identity 权限标识
     * @return 权限
     */
    public PermissionEntity getPermissionByIdentity(String identity) {
        return repository.getByIdentity(identity);
    }

    @Override
    protected void beforeDelete(@NotNull PermissionEntity permission) {
        FORBIDDEN_DELETE.when(permission.getIsSystem(), "系统内置权限无法被删除!");
        TreeUtil.ensureNoChildrenBeforeDelete(permission.getId(), id -> filter(new PermissionEntity().setParentId(id)));
    }

    @Override
    protected @NotNull List<PermissionEntity> afterGetList(@NotNull List<PermissionEntity> list) {
        list.forEach(PermissionEntity::excludeNotMeta);
        return list;
    }

    public void initMcpToolPermission(@NotNull List<McpTool> list) {
        PermissionEntity parent = new PermissionEntity()
                .setName("MCP工具")
                .setIdentity("mcp:tools")
                .setType(PermissionType.MCP.getKey())
                .setIsSystem(true);
        long parentId = add(parent);
        for (McpTool mcpTool : list) {
            add(new PermissionEntity()
                    .setName(mcpTool.getName())
                    .setIdentity(McpService.getPermissionIdentity(mcpTool))
                    .setType(PermissionType.MCP.getKey())
                    .setIsSystem(true)
                    .setParentId(parentId));
        }
    }

    public void loadPermission() {
        List<PermissionEntity> permissions = PermissionUtil.scanPermission(Application.class.getPackageName(), PermissionEntity.class);
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
