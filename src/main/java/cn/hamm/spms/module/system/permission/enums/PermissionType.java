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
     * <p>
     * 原先还有一个 {@code MCP(1, "MCP 权限")}，但 MCP 模块已整体下线
     * （{@code module/mcp} 目录不存在），留着只会让前端渲染出一个点进去空白的菜单。
     * 数据库是按 {@code name()} 存字符串的，删除本枚举项不影响已有数据。
     * </p>
     */
    API(0, "API 权限");

    private final int key;
    private final String label;
}
