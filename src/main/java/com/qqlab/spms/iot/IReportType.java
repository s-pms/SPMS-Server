package com.qqlab.spms.iot;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>采集类型</h1>
 *
 * @author Hamm
 */

@Getter
@AllArgsConstructor
public enum IReportType implements IEnum {
    /**
     * <h2>设备</h2>
     */
    DEVICE(1, "设备"),

    /**
     * <h2>网关</h2>
     */
    GATEWAY(2, "网关"),

    ;

    private final int value;
    private final String label;
}
