package cn.hamm.spms.module.asset.device.enums;

import cn.hamm.airpower.dictionary.IDictionary;
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
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 报警
     */
    ALARM(1, "报警"),

    /**
     * 运行
     */
    RUNNING(2, "运行"),

    /**
     * 空闲
     */
    FREE(3, "空闲"),

    /**
     * 关机
     */
    DOW(4, "关机"),

    /**
     * 调试
     */
    DEBUG(5, "调试"),
    ;

    private final int key;
    private final String label;
}
