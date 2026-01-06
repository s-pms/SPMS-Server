package cn.hamm.spms.module.system.permission.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>权限类别</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum PermissionType implements IDictionary {
    /**
     * API 权限
     */
    API(0, "API 权限"),

    /**
     * MCP 权限
     */
    MCP(1, "MCP 权限");

    private final int key;
    private final String label;
}
