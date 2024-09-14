package cn.hamm.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>实体</h1>
 *
 * @author zfy
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Description("工艺路线工序-物料")
@Table(name = "craft_router_operation_material")
public class CraftRouterOperationMaterialEntity extends BaseEntity {
    @Description("工艺路线工序id")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工艺路线工序id不能为空")
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '工艺路线工序id'")
    private Long routerOperationId;

    @Description("物料")
    @ManyToOne(fetch = FetchType.EAGER)
    private MaterialEntity material;

    @Description("装配数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '装配数量'")
    private Double quantity;

}
