package com.qqlab.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.asset.device.DeviceEntity;
import jakarta.persistence.*;
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
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Description("工艺路线工序-设备")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "craft_router_opertion_device")
@EqualsAndHashCode(callSuper = true)
public class CraftRouterOperationDeviceEntity extends BaseEntity<CraftRouterOperationDeviceEntity> {

    @Description("工艺路线工序id")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工艺路线工序id不能为空")
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '工艺路线工序id'")
    private Long routerOperationId;

    @Payload
    @Description("设备")
    @ManyToOne(fetch = FetchType.EAGER)
    private DeviceEntity device;

}
