package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则表格枚举</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum CodeRuleField implements IDictionary {
    /**
     * 角色
     */
    RoleCode(1, "角色编码", "RO"),

    /**
     * 供应商编码
     */
    SupplierCode(2, "供应商编码", "SUP"),


    /**
     * 存储资源编码
     */
    StorageCode(3, "存储资源编码", "SRG"),

    /**
     * 工厂结构编码
     */
    StructureCode(4, "工厂结构编码", "ST"),

    /**
     * 客户编码
     */
    CustomerCode(5, "客户编码", "CT"),

    /**
     * 物料编码
     */
    MaterialCode(6, "物料编码", "MA"),

    /**
     * 单位编码
     */
    UnitCode(7, "单位编码", "UT"),

    /**
     * 采购单号
     */
    PurchaseBillCode(8, "采购单号", "PC"),

    /**
     * 销售单号
     */
    SaleBillCode(9, "销售单号", "SL"),

    /**
     * 生产计划号
     */
    PlanBillCode(10, "生产计划号", "PL"),

    /**
     * 生产订单号
     */
    OrderBillCode(11, "生产订单号", "ODR"),

    /**
     * 领料单号
     */
    PickoutBillCode(12, "领料单号", "PK"),

    /**
     * 退料单号
     */
    RestoreBillCode(13, "退料单号", "RT"),

    /**
     * 入库单号
     */
    InputBillCode(14, "入库单号", "IN"),

    /**
     * 出库单号
     */
    OutputBillCode(15, "出库单号", "OUT"),

    /**
     * 移库单号
     */
    MoveBillCode(16, "移库单号", "MV"),

    /**
     * 设备编码
     */
    DeviceCode(17, "设备编码", "DE"),

    /**
     * 工序编码
     */
    OperationCode(18, "工序编码", "OP");

    private final int key;
    private final String label;
    private final String defaultPrefix;
}
