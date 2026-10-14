package cn.hamm.spms.module.iot.report.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>上报数据类型</h1>
 *
 * @author Hamm.cn
 * @apiNote 同时是 {@code ParameterEntity#dataType} 的字典来源，决定了 InfluxDB 查询时
 * 走 {@code aggregateWindow} 聚合还是 {@code limit} 取原始点
 */
@AllArgsConstructor
@Getter
public enum ReportDataType implements IDictionary {
    /**
     * 数量：可聚合的数值（如产量），查询时按颗粒度求均值
     */
    NUMBER(1, "数量"),

    /**
     * 状态：离散状态量（如运行状态、报警），查询时取原始点不聚合
     */
    STATUS(2, "状态"),

    /**
     * 开关：布尔量，取原始点不聚合
     */
    BOOLEAN(3, "开关"),

    /**
     * 信息：文本，取原始点不聚合
     */
    STRING(4, "信息"),
    ;
    private final int key;
    private final String label;
}
