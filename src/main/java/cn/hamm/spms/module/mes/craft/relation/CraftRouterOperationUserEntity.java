package cn.hamm.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
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
@Description("工艺路线工序-人员")
@Table(name = "craft_router_operation_user")
public class CraftRouterOperationUserEntity extends BaseEntity<CraftRouterOperationUserEntity> {
    @Description("工艺路线工序id")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工艺路线工序id不能为空")
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '工艺路线工序id'")
    private Long routerOperationId;

    @Payload
    @Description("人员")
    @ManyToOne(fetch = FetchType.EAGER)
    private UserEntity user;

}
