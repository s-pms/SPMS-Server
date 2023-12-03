package com.qqlab.spms.module.iot.collection;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>采集数据实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "connection")
@Description("采集数据")
public class CollectionEntity extends BaseEntity<CollectionEntity> {
    @Description("参数编码")
    @Column(columnDefinition = "varchar(255) default '' comment '参数编码'")
    private String code;

    @Description("设备UUID")
    @Column(columnDefinition = "varchar(255) default '' comment '设备UUID'")
    private String uuid;

    @Description("采集数据")
    @Column(columnDefinition = "text comment '采集数据'")
    private String value;

    @Transient
    private String label;

    /**
     * <h2>值的布尔值</h2>
     *
     * @return 布尔值
     */
    public boolean parseBoolValue() {
        return Integer.parseInt(value) == 1;
    }

    /**
     * <h2>值的数值</h2>
     *
     * @return 数值
     */
    public double parseNumberValue() {
        return Double.parseDouble(value);
    }
}
