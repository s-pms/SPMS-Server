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
     * <h3>未知</h3>
     */
    UNKNOWN(0, "未知"),

    /**
     * <h3>报警</h3>
     */
    ALARM(1, "报警"),

    /**
     * <h3>运行</h3>
     */
    RUNNING(2, "运行"),

    /**
     * <h3>空闲</h3>
     */
    FREE(3, "空闲"),

    /**
     * <h3>关机</h3>
     */
    DOW(4, "关机"),

    /**
     * <h3>调试</h3>
     */
    DEBUG(5, "调试"),
    ;

    private final int key;
    private final String label;
}
