package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.interfaces.IDictionary;
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
     * <h3>API权限</h3>
     */
    API(0, "API权限"),

    /**
     * <h3>MCP权限</h3>
     */
    MCP(1, "MCP权限");

    private final int key;
    private final String label;
}
