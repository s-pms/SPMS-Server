package cn.hamm.spms.module.system.coderule.enums;

import cn.hamm.airpower.web.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

import static cn.hamm.spms.module.system.coderule.enums.SerialNumberUpdate.*;

/**
 * <h1>编码规则表格枚举</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum CodeRuleField implements IDictionary {
    /**
     * 角色
     */
    RoleCode(1, "角色编码", "RO", NEVER),

    /**
     * 供应商编码
     */
    SupplierCode(2, "供应商编码", "SUP", YEAR),

    /**
     * 仓库编码
     */
    StorageCode(3, "仓库编码", "SRG", NEVER),

    /**
     * 生产单元编码
     */
    StructureCode(4, "生产单元编码", "ST", NEVER),

    /**
     * 客户编码
     */
    CustomerCode(5, "客户编码", "CT", YEAR),

    /**
     * 物料编码
     */
    MaterialCode(6, "物料编码", "MA", YEAR),

    /**
     * 单位编码
     */
    UnitCode(7, "单位编码", "UT", NEVER),

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
    PickingBillCode(12, "领料单号", "PK"),

    /**
     * 退料单号
     */
    RestoreBillCode(13, "退料单号", "RET"),

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
    DeviceCode(17, "设备编码", "DE", MONTH),

    /**
     * 工序编码
     */
    OperationCode(18, "工序编码", "OP", YEAR),

    /**
     * 部门编码
     */
    DepartmentCode(19, "部门编码", "DP", NEVER),

    /**
     * BOM 编码
     */
    BomCode(20, "配方编码", "BOM", NEVER),

    /**
     * 工艺编码
     */
    RoutingCode(21, "工艺编码", "RT", NEVER),

    /**
     * 合同编码
     */
    ContractCode(22, "合同编码", "CON", MONTH),

    ;

    private final int key;
    private final String label;

    /**
     * 默认前缀
     */
    private final String defaultPrefix;

    /**
     * 默认序列号类型
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
