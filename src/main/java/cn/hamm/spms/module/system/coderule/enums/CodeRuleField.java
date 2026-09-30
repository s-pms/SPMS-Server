package cn.hamm.spms.module.system.coderule.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

import static cn.hamm.spms.module.system.coderule.enums.SerialNumberUpdate.*;

/**
 * <h1>编码规则字段</h1>
 *
 * @author Hamm.cn
 * @apiNote 每种业务编码在此登记一项：{@code defaultPrefix} 是新建规则时的默认前缀，
 * {@code defaultSnType} 是流水号重置周期（{@link SerialNumberUpdate}）。
 * dev 模式重启时据此初始化默认规则，实体上通过
 * {@code @AutoGenerateCode(CodeRuleField.XXX)} 引用
 */
@Getter
@AllArgsConstructor
public enum CodeRuleField implements IDictionary {
    /**
     * 角色编码，默认前缀 RO
     */
    RoleCode(1, "角色编码", "RO", NEVER),

    /**
     * 供应商编码，默认前缀 SUP，流水号每年重置
     */
    SupplierCode(2, "供应商编码", "SUP", YEAR),

    /**
     * 仓库编码，默认前缀 SRG
     */
    StorageCode(3, "仓库编码", "SRG", NEVER),

    /**
     * 生产单元编码，默认前缀 ST
     */
    StructureCode(4, "生产单元编码", "ST", NEVER),

    /**
     * 客户编码，默认前缀 CT，流水号每年重置
     */
    CustomerCode(5, "客户编码", "CT", YEAR),

    /**
     * 物料编码，默认前缀 MA，流水号每年重置
     */
    MaterialCode(6, "物料编码", "MA", YEAR),

    /**
     * 计量单位编码，默认前缀 UT
     */
    UnitCode(7, "单位编码", "UT", NEVER),

    /**
     * 采购单号，默认前缀 PC
     */
    PurchaseBillCode(8, "采购单号", "PC"),

    /**
     * 销售单号，默认前缀 SL
     */
    SaleBillCode(9, "销售单号", "SL"),

    /**
     * 生产计划号，默认前缀 PL
     */
    PlanBillCode(10, "生产计划号", "PL"),

    /**
     * 生产订单号，默认前缀 ODR
     */
    OrderBillCode(11, "生产订单号", "ODR"),

    /**
     * 生产领料单号，默认前缀 PK
     */
    PickingBillCode(12, "领料单号", "PK"),

    /**
     * 生产退料单号，默认前缀 RET
     */
    RestoreBillCode(13, "退料单号", "RET"),

    /**
     * 采购入库单号，默认前缀 IN
     */
    InputBillCode(14, "入库单号", "IN"),

    /**
     * 销售出库单号，默认前缀 OUT
     */
    OutputBillCode(15, "出库单号", "OUT"),

    /**
     * 仓库移库单号，默认前缀 MV
     */
    MoveBillCode(16, "移库单号", "MV"),

    /**
     * 设备资产编码，默认前缀 DE，流水号每月重置
     */
    DeviceCode(17, "设备编码", "DE", MONTH),

    /**
     * 工序编码，默认前缀 OP，流水号每年重置
     */
    OperationCode(18, "工序编码", "OP", YEAR),

    /**
     * 部门编码，默认前缀 DP
     */
    DepartmentCode(19, "部门编码", "DP", NEVER),

    /**
     * BOM 配方编码，默认前缀 BOM
     */
    BomCode(20, "配方编码", "BOM", NEVER),

    /**
     * 工艺路线编码，默认前缀 RT
     */
    RoutingCode(21, "工艺编码", "RT", NEVER),

    /**
     * 销售合同编码，默认前缀 CON，流水号每月重置
     */
    ContractCode(22, "合同编码", "CON", MONTH),

    ;

    private final int key;
    private final String label;

    /**
     * 初始化规则时使用的默认前缀
     */
    private final String defaultPrefix;

    /**
     * 初始化规则时使用的默认流水号重置周期
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
