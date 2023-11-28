package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.mes.order.OrderStatus;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zfy
 * @date 2023/11/28
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "device")
@Description("设备")
public class DeviceEntity extends BaseEntity<DeviceEntity> {

    /**
     * <h2>设备名称</h2>
     */
    @Description("设备名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '设备名称'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "设备名称不能为空")
    private String name;

    @Description("设备编码")
    @Column(columnDefinition = "varchar(255) default '' comment '设备编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.DeviceCode)
    private String code;


    @Description("设备状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '设备状态'")
    @Dictionary(DeviceStatus.class)
    private Integer status;

}
