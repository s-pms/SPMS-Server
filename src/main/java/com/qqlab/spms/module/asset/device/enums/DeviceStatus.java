package com.qqlab.spms.module.asset.device.enums;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>设备状态</h1>
 *
 * @author zfy
 */
@AllArgsConstructor
@Getter
public enum DeviceStatus implements IEnum {
    /**
     * <h2>报警</h2>
     */
    ALARM(1, "报警"),

    /**
     * <h2>运行</h2>
     */
    RUNNING(2, "运行"),

    /**
     * <h2>空闲</h2>
     */
    FREE(3, "空闲"),

    /**
     * <h2>关机</h2>
     */
    DOW(4, "关机"),

    /**
     * <h2>调试</h2>
     */
    DEBUG(5, "调试"),
    ;


    @Getter
    private final int value;

    @Getter
    private final String label;
}
