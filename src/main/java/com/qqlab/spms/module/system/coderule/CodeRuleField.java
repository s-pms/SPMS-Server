package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则表格枚举</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum CodeRuleField implements IEnum {

    /**
     * <h2>角色</h2>
     */
    RoleCode(1, "角色编码", "RO"),


    /**
     * <h2>供应商编码</h2>
     */
    SupplierCode(2, "供应商编码", "SUP"),


    /**
     * <h2>存储资源编码</h2>
     */
    StorageCode(3, "存储资源编码", "SRG"),

    /**
     * <h2>工厂结构编码</h2>
     */
    StructureCode(4, "工厂结构编码", "ST"),

    /**
     * <h2>客户编码</h2>
     */
    CustomerCode(5, "客户编码", "CT"),

    /**
     * <h2>物料编码</h2>
     */
    MaterialCode(6, "物料编码", "MA"),

    /**
     * <h2>单位编码</h2>
     */
    UnitCode(7, "单位编码", "UT"),

    /**
     * <h2>采购单号</h2>
     */
    PurchaseBillCode(8, "采购单号", "PC"),

    /**
     * <h2>销售单号</h2>
     */
    SaleBillCode(9, "销售单号", "SL"),

    /**
     * <h2>生产计划号</h2>
     */
    PlanBillCode(10, "生产计划号", "PL"),

    /**
     * <h2>生产订单号</h2>
     */
    OrderBillCode(11, "生产订单号", "ODR"),

    /**
     * <h2>领料单号</h2>
     */
    PickoutBillCode(12, "领料单号", "PK"),

    /**
     * <h2>退料单号</h2>
     */
    RestoreBillCode(13, "退料单号", "RT");

    @Getter
    private int value;

    @Getter
    private String label;

    @Getter
    private String defaultPrefix;
}
