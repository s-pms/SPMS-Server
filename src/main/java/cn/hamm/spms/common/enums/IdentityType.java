package cn.hamm.spms.common.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>身份类型枚举</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum IdentityType implements IDictionary {
    /**
     * <h3>其他</h3>
     */
    OTHER(0, "其他"),

    /**
     * <h3>个人</h3>
     */
    PERSONAL(1, "个人"),

    /**
     * <h3>企业</h3>
     */
    COMPANY(2, "企业"),
    ;

    private final int key;
    private final String label;
}
