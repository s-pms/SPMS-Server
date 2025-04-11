package cn.hamm.spms.common.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>证件类型枚举</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum CertificateType implements IDictionary {
    /**
     * <h3>其他</h3>
     */
    OTHER(0, "其他", IdentityType.OTHER),

    /**
     * <h3>中国身份证</h3>
     */
    CHINESE_ID_CARD(1, "中国身份证", IdentityType.PERSONAL),

    /**
     * <h3>护照</h3>
     */
    PASSPORT(2, "护照", IdentityType.PERSONAL),

    /**
     * <h3>港澳通行证</h3>
     */
    HONGKONG_MACAO_PASSPORT(3, "港澳通行证", IdentityType.PERSONAL),

    /**
     * <h3>台湾通行证</h3>
     */
    TAIWAN_PASSPORT(4, "台湾通行证", IdentityType.PERSONAL),

    /**
     * <h3>统一信用代码</h3>
     */
    UNIFIED_CREDIT_CODE(5, "统一信用代码", IdentityType.COMPANY),

    ;

    private final int key;
    private final String label;
    private final IdentityType participantType;
}
