package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则表格枚举</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum CodeRuleField implements IDictionary {
    /**
     * <h3>角色</h3>
     */
    RoleCode(1, "角色编码", "RO", SerialNumberUpdate.NEVER, Constant.EMPTY_STRING),

    /**
     * <h3>供应商编码</h3>
     */
    SupplierCode(2, "供应商编码", "SUP", SerialNumberUpdate.YEAR, "yyyy"),

    /**
     * <h3>仓库编码</h3>
     */
    StorageCode(3, "仓库编码", "SRG", SerialNumberUpdate.NEVER, Constant.EMPTY_STRING),

    /**
     * <h3>生产单元编码</h3>
     */
    StructureCode(4, "生产单元编码", "ST", SerialNumberUpdate.NEVER, Constant.EMPTY_STRING),

    /**
     * <h3>客户编码</h3>
     */
    CustomerCode(5, "客户编码", "CT", SerialNumberUpdate.YEAR, "yyyy"),

    /**
     * <h3>物料编码</h3>
     */
    MaterialCode(6, "物料编码", "MA", SerialNumberUpdate.YEAR, "yyyy"),

    /**
     * <h3>单位编码</h3>
     */
    UnitCode(7, "单位编码", "UT", SerialNumberUpdate.NEVER, Constant.EMPTY_STRING),

    /**
     * <h3>采购单号</h3>
     */
    PurchaseBillCode(8, "采购单号", "PC", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>销售单号</h3>
     */
    SaleBillCode(9, "销售单号", "SL", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>生产计划号</h3>
     */
    PlanBillCode(10, "生产计划号", "PL", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>生产订单号</h3>
     */
    OrderBillCode(11, "生产订单号", "ODR", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>领料单号</h3>
     */
    PickingBillCode(12, "领料单号", "PK", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>退料单号</h3>
     */
    RestoreBillCode(13, "退料单号", "RET", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>入库单号</h3>
     */
    InputBillCode(14, "入库单号", "IN", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>出库单号</h3>
     */
    OutputBillCode(15, "出库单号", "OUT", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>移库单号</h3>
     */
    MoveBillCode(16, "移库单号", "MV", SerialNumberUpdate.DAY, "yyyymmdd"),

    /**
     * <h3>设备编码</h3>
     */
    DeviceCode(17, "设备编码", "DE", SerialNumberUpdate.MONTH, "yyyymm"),

    /**
     * <h3>工序编码</h3>
     */
    OperationCode(18, "工序编码", "OP", SerialNumberUpdate.YEAR, "yyyy"),

    /**
     * <h3>部门编码</h3>
     */
    DepartmentCode(19, "部门编码", "DP", SerialNumberUpdate.NEVER, Constant.EMPTY_STRING),

    /**
     * <h3>BOM编码</h3>
     */
    BomCode(20, "配方编码", "BOM", SerialNumberUpdate.NEVER, Constant.EMPTY_STRING),

    /**
     * <h3>工艺编码</h3>
     */
    RoutingCode(21, "工艺编码", "RT", SerialNumberUpdate.NEVER, Constant.EMPTY_STRING),

    ;

    private final int key;
    private final String label;

    /**
     * <h3>默认前缀</h3>
     */
    private final String defaultPrefix;

    /**
     * <h3>默认序列号类型</h3>
     */
    private final SerialNumberUpdate defaultSnType;

    /**
     * <h3>默认模板</h3>
     */
    private final String defaultTemplate;
}
