package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则表格枚举</h1>
 *
 * @author Hamm
 */
@Getter
@AllArgsConstructor
public enum CodeRuleField implements IDictionary {
    /**
     * <h2>角色</h2>
     */
    RoleCode(1, "角色编码", "RO", SerialNumberUpdate.NEVER, ""),

    /**
     * <h2>供应商编码</h2>
     */
    SupplierCode(2, "供应商编码", "SUP", SerialNumberUpdate.YEAR, "yyyy"),

    /**
     * <h2>存储资源编码</h2>
     */
    StorageCode(3, "存储资源编码", "SRG", SerialNumberUpdate.NEVER, ""),

    /**
     * <h2>工厂结构编码</h2>
     */
    StructureCode(4, "工厂结构编码", "ST", SerialNumberUpdate.NEVER, ""),

    /**
     * <h2>客户编码</h2>
     */
    CustomerCode(5, "客户编码", "CT", SerialNumberUpdate.YEAR, "yyyy"),

    /**
     * <h2>物料编码</h2>
     */
    MaterialCode(6, "物料编码", "MA", SerialNumberUpdate.YEAR, "yyyy"),

    /**
     * <h2>单位编码</h2>
     */
    UnitCode(7, "单位编码", "UT", SerialNumberUpdate.NEVER, ""),

    /**
     * <h2>采购单号</h2>
     */
    PurchaseBillCode(8, "采购单号", "PC", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>销售单号</h2>
     */
    SaleBillCode(9, "销售单号", "SL", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>生产计划号</h2>
     */
    PlanBillCode(10, "生产计划号", "PL", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>生产订单号</h2>
     */
    OrderBillCode(11, "生产订单号", "ODR", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>领料单号</h2>
     */
    PickoutBillCode(12, "领料单号", "PK", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>退料单号</h2>
     */
    RestoreBillCode(13, "退料单号", "RT", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>入库单号</h2>
     */
    InputBillCode(14, "入库单号", "IN", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>出库单号</h2>
     */
    OutputBillCode(15, "出库单号", "OUT", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>移库单号</h2>
     */
    MoveBillCode(16, "移库单号", "MV", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h2>设备编码</h2>
     */
    DeviceCode(17, "设备编码", "DE", SerialNumberUpdate.MONTH, "yyyymm"),

    /**
     * <h2>工序编码</h2>
     */
    OperationCode(18, "工序编码", "OP", SerialNumberUpdate.YEAR, "yyyy");

    private final int key;
    private final String label;

    /**
     * <h2>默认前缀</h2>
     */
    private final String defaultPrefix;

    /**
     * <h2>默认序列号类型</h2>
     */
    private final SerialNumberUpdate defaultSnType;

    /**
     * <h2>默认模板</h2>
     */
    private final String defaultTemplate;
}
