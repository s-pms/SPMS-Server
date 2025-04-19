package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

import static cn.hamm.spms.module.system.coderule.SerialNumberUpdate.*;

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
    RoleCode(1, "角色编码", "RO", NEVER),

    /**
     * <h3>供应商编码</h3>
     */
    SupplierCode(2, "供应商编码", "SUP", YEAR),

    /**
     * <h3>仓库编码</h3>
     */
    StorageCode(3, "仓库编码", "SRG", NEVER),

    /**
     * <h3>生产单元编码</h3>
     */
    StructureCode(4, "生产单元编码", "ST", NEVER),

    /**
     * <h3>客户编码</h3>
     */
    CustomerCode(5, "客户编码", "CT", YEAR),

    /**
     * <h3>物料编码</h3>
     */
    MaterialCode(6, "物料编码", "MA", YEAR),

    /**
     * <h3>单位编码</h3>
     */
    UnitCode(7, "单位编码", "UT", NEVER),

    /**
     * <h3>采购单号</h3>
     */
    PurchaseBillCode(8, "采购单号", "PC"),

    /**
     * <h3>销售单号</h3>
     */
    SaleBillCode(9, "销售单号", "SL"),

    /**
     * <h3>生产计划号</h3>
     */
    PlanBillCode(10, "生产计划号", "PL"),

    /**
     * <h3>生产订单号</h3>
     */
    OrderBillCode(11, "生产订单号", "ODR"),

    /**
     * <h3>领料单号</h3>
     */
    PickingBillCode(12, "领料单号", "PK"),

    /**
     * <h3>退料单号</h3>
     */
    RestoreBillCode(13, "退料单号", "RET"),

    /**
     * <h3>入库单号</h3>
     */
    InputBillCode(14, "入库单号", "IN"),

    /**
     * <h3>出库单号</h3>
     */
    OutputBillCode(15, "出库单号", "OUT"),

    /**
     * <h3>移库单号</h3>
     */
    MoveBillCode(16, "移库单号", "MV"),

    /**
     * <h3>设备编码</h3>
     */
    DeviceCode(17, "设备编码", "DE", MONTH),

    /**
     * <h3>工序编码</h3>
     */
    OperationCode(18, "工序编码", "OP", YEAR),

    /**
     * <h3>部门编码</h3>
     */
    DepartmentCode(19, "部门编码", "DP", NEVER),

    /**
     * <h3>BOM编码</h3>
     */
    BomCode(20, "配方编码", "BOM", NEVER),

    /**
     * <h3>工艺编码</h3>
     */
    RoutingCode(21, "工艺编码", "RT", NEVER),

    /**
     * <h3>合同编码</h3>
     */
    ContractCode(22, "合同编码", "CON", YEAR),

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

    @Contract(pure = true)
    CodeRuleField(int key, String label, String defaultPrefix) {
        this.key = key;
        this.label = label;
        this.defaultPrefix = defaultPrefix;
        this.defaultSnType = DAY;
    }
}
