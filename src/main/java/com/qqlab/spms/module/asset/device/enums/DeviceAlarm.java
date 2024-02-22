package com.qqlab.spms.module.asset.device.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>设备报警状态</h1>
 *
 * @author Hamm
 */
@Getter
@AllArgsConstructor
public enum DeviceAlarm implements IDictionary {
    /**
     * 正常
     */
    NONE(0, "正常"),

    /**
     * 系统报警
     */
    SYSTEM(1, "系统报警"),

    /**
     * 手动报警
     */
    MANUAL(2, "手动报警"),

    /**
     * 规则报警
     */
    RULE(3, "规则报警"),
    ;

    private final int key;
    private final String label;
}
