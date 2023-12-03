package com.qqlab.spms.iot.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <h1>Payload数据</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class PayloadData {
    /**
     * <h2>Payload数据键</h2>
     */
    private String key;

    /**
     * <h2>Payload数据值</h2>
     */
    private String value;

    /**
     * 转换数据为布尔
     *
     * @return 值
     */
    public boolean transformBoolean() {
        return "1".equals(value);
    }

    /**
     * 转换数据为数字
     *
     * @return 值
     */
    public double transformNumber() {
        return Double.parseDouble(value);
    }
}
