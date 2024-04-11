package cn.hamm.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zfy
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "craft_router_operation_device")
@Description("工艺路线工序-设备")
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
