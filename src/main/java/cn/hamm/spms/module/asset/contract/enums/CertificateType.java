package cn.hamm.spms.module.asset.contract.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>证件类型</h1>
 *
 * @author Hamm.cn
 * @apiNote 每项的第三个构造参数是对应的 {@link IdentityType}，新增证件类型时必须一并指定，
 * 否则参与方会落到「其他」身份上
 */
@AllArgsConstructor
@Getter
public enum CertificateType implements IDictionary {
    /**
     * 其他
     */
    OTHER(0, "其他", IdentityType.OTHER),

    /**
     * 中国身份证
     */
    CHINESE_ID_CARD(1, "中国身份证", IdentityType.PERSONAL),

    /**
     * 护照
     */
    PASSPORT(2, "护照", IdentityType.PERSONAL),

    /**
     * 港澳通行证
     */
    HONGKONG_MACAO_PASSPORT(3, "港澳通行证", IdentityType.PERSONAL),

    /**
     * 台湾通行证
     */
    TAIWAN_PASSPORT(4, "台湾通行证", IdentityType.PERSONAL),

    /**
     * 统一信用代码
     */
    UNIFIED_CREDIT_CODE(5, "统一信用代码", IdentityType.COMPANY),

    ;

    private final int key;
    private final String label;
    private final IdentityType identityType;
}
