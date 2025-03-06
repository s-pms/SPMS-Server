package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static cn.hamm.spms.module.system.config.ConfigType.BOOLEAN;

/**
 * <h1>系统配置</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
@AllArgsConstructor
public enum ConfigFlag implements IDictionary {
    AUTO_REGISTER_EMAIL_LOGIN(1, "邮箱登录时自动注册", BOOLEAN, "1"),
    ORDER_AUTO_FINISH(11, "生产订单开启自动标记生产完成", BOOLEAN, "1"),
    ORDER_MANUAL_FINISH(12, "生产订单允许手动标记生产完成", BOOLEAN, "1"),
    INPUT_BILL_AUTO_AUDIT(13, "入库单开启自动审核", BOOLEAN, "0"),
    OUTPUT_BILL_AUTO_AUDIT(14, "出库单开启自动审核", BOOLEAN, "0"),
    MOVE_BILL_AUTO_AUDIT(15, "移库单开启自动审核", BOOLEAN, "0"),
    PURCHASE_BILL_AUTO_AUDIT(16, "采购单开启自动审核", BOOLEAN, "0"),
    SALE_BILL_AUTO_AUDIT(17, "销售单开启自动审核", BOOLEAN, "0"),
    ORDER_ENABLE_SUBMIT_WORK(18, "订单报工模式", BOOLEAN, "1"),
    BOM_AUTO_AUDIT(19, "生产配方自动审核", BOOLEAN, "0"),
    PLAN_AUTO_FINISH(20, "生产计划开启自动完成", BOOLEAN, "1"),
    ORDER_AUTO_START_AFTER_AUDIT(21, "订单审核后自动开始生产", BOOLEAN, "0"),
    PICKING_BILL_AUTO_AUDIT(22, "领料单开启自动审核", BOOLEAN, "0"),

    ;

    private final int key;
    private final String label;
    private final ConfigType type;
    private final String defaultValue;
}
