package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.dictionary.IDictionary;
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
     * API权限
     */
    API(0, "API权限"),

    /**
     * MCP权限
     */
    MCP(1, "MCP权限");

    private final int key;
    private final String label;
}
