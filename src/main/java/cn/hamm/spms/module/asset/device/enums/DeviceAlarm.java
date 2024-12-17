package cn.hamm.spms.module.asset.device.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>设备报警状态</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum DeviceAlarm implements IDictionary {
    /**
     * <h3>正常</h3>
     */
    NONE(0, "正常"),

    /**
     * <h3>系统报警</h3>
     */
    SYSTEM(1, "系统报警"),

    /**
     * <h3>手动报警</h3>
     */
    MANUAL(2, "手动报警"),

    /**
     * <h3>规则报警</h3>
     */
    RULE(3, "规则报警"),
    ;

    private final int key;
    private final String label;
}
