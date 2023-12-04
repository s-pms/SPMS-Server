package com.qqlab.spms.module.asset.device.enums;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>设备报警状态</h1>
 *
 * @author Hamm
 */
@Getter
@AllArgsConstructor
public enum DeviceAlarm implements IEnum {
    /**
     * <h2>正常</h2>
     */
    NONE(0, "正常"),

    /**
     * <h2>系统报警</h2>
     */
    SYSTEM(1, "系统报警"),

    /**
     * <h2>手动报警</h2>
     */
    MANUAL(2, "手动报警"),

    /**
     * <h2>规则报警</h2>
     */
    RULE(3, "规则报警"),
    ;

    private final int value;

    private final String label;
}