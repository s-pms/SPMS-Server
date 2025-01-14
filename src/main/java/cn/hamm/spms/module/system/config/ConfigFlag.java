package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>系统配置</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
@AllArgsConstructor
public enum ConfigFlag implements IDictionary {
    ORDER_AUTO_FINISH(1, "生产订单开启自动完成", ConfigType.BOOLEAN, "1"),
    ORDER_MANUAL_FINISH(2, "生产订单允许手动完成", ConfigType.BOOLEAN, "1"),
    INPUT_ORDER_AUTO_AUDIT(3, "入库单开启自动审核", ConfigType.BOOLEAN, "0"),
    OUTPUT_ORDER_AUTO_AUDIT(4, "出库单开启自动审核", ConfigType.BOOLEAN, "0"),
    MOVE_ORDER_AUTO_AUDIT(5, "移库单开启自动审核", ConfigType.BOOLEAN, "0"),
    PURCHASE_ORDER_AUTO_AUDIT(6, "采购单开启自动审核", ConfigType.BOOLEAN, "0"),
    SALE_ORDER_AUTO_AUDIT(7, "销售单开启自动审核", ConfigType.BOOLEAN, "0"),
    ORDER_ENABLE_SUBMIT_WORK(8, "订单报工模式", ConfigType.BOOLEAN, "1"),
    ;

    private final int key;
    private final String label;
    private final ConfigType type;
    private final String defaultValue;
}
