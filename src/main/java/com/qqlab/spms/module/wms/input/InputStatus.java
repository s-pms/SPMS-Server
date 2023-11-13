package com.qqlab.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>生产计划状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum InputStatus implements IEnum {
    /**
     * <h2>审核中</h2>
     */
    AUDITING(1, "审核中"),

    /**
     * <h2>已驳回</h2>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h2>入库中</h2>
     */
    PRODUCING(3, "入库中"),

    /**
     * <h2>已完成</h2>
     */
    DONE(4, "已完成"),

    /**
     * <h2>已取消</h2>
     */
    CANCELED(5, "已取消");


    @Getter
    private int value;

    @Getter
    private String label;
}
