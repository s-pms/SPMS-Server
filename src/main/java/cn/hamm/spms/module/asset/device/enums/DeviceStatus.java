package cn.hamm.spms.module.asset.device.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>设备状态</h1>
 *
 * @author zfy
 */
@AllArgsConstructor
@Getter
public enum DeviceStatus implements IDictionary {
    /**
     * <h2>未知</h2>
     */
    UNKNOWN(0, "未知"),

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

    private final int key;
    private final String label;
}
