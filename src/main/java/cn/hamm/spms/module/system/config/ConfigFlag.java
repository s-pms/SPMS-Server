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
    AUTO_REGISTER_EMAIL_LOGIN(1, "邮箱自动注册", BOOLEAN, "1", "邮箱验证码登录时，如果未注册则自动注册后登陆"),
    ORDER_AUTO_FINISH(11, "订单自动标记完成", BOOLEAN, "1", "完成数量超过计划数量时，订单将自动被标记为完成"),
    ORDER_MANUAL_FINISH(12, "订单手动标记完成", BOOLEAN, "1", "允许在任何情况下手动完成订单"),
    INPUT_BILL_AUTO_AUDIT(13, "入库单自动审核", BOOLEAN, "0", "开启此项后，创建入库单则进入入库中状态"),
    OUTPUT_BILL_AUTO_AUDIT(14, "出库单自动审核", BOOLEAN, "0", "开启此项后，创建出库单则进入出库中状态"),
    MOVE_BILL_AUTO_AUDIT(15, "移库单自动审核", BOOLEAN, "0", "开启此项后，创建移库单则进入移库中状态"),
    PURCHASE_BILL_AUTO_AUDIT(16, "采购单自动审核", BOOLEAN, "0", "开启此项后，创建采购单则进入采购中状态"),
    SALE_BILL_AUTO_AUDIT(17, "销售单开启自动审核", BOOLEAN, "0", "开启此项后，创建销售单则进入销售中状态"),
    ORDER_ENABLE_SUBMIT_WORK(18, "订单报工模式", BOOLEAN, "1", "开启订单报工模式后，工序将无法进行报工，此项开启则表示为粗放型报工管理模式"),
    ORDER_AUTO_START_AFTER_AUDIT(19, "订单审核后自动开始", BOOLEAN, "0", "开启此项后，订单创建即开始进入生产环节"),
    PICKING_BILL_AUTO_AUDIT(20, "领料单自动审核", BOOLEAN, "0", "开启此项后，创建领料单则进入领料中状态"),

    ;

    private final int key;
    private final String label;
    private final ConfigType type;
    private final String defaultValue;
    private final String description;
}
