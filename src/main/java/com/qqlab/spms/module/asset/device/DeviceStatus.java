package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zfy
 * @date 2023/11/28
 * 设备状态
 */
@AllArgsConstructor
public enum DeviceStatus implements IEnum {
    /**
     * <h2>未知"</h2>
     */
    UNKNOWN(0, "未知"),

    /**
     * <h2>运行中</h2>
     */
    RUNNING(1, "运行中"),
    /**
     * <h2>停机，暂停"</h2>
     */
    HALT(2, "停机"),

    /**
     * <h2>故障</h2>
     */
    FAULT(3, "故障"),

    /**
     * <h2>关机</h2>
     */
    SHUTDOWN(4, "关机");


    @Getter
    private final int value;

    @Getter
    private final String label;
}
