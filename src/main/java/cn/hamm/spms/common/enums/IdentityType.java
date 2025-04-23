package cn.hamm.spms.common.enums;

import cn.hamm.airpower.dictionary.IDictionary;
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
     * 其他
     */
    OTHER(0, "其他"),

    /**
     * 个人
     */
    PERSONAL(1, "个人"),

    /**
     * 企业
     */
    COMPANY(2, "企业"),
    ;

    private final int key;
    private final String label;
}
