package com.qqlab.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.asset.material.MaterialEntity;
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
 * @date 2023/12/25
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Description("工艺路线工序-物料")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "craft_router_opertion_material")
@EqualsAndHashCode(callSuper = true)
public class CraftRouterOperationMaterialEntity extends BaseEntity<CraftRouterOperationMaterialEntity> {

    @Description("工艺路线工序id")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工艺路线工序id不能为空")
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '工艺路线工序id'")
    private Long routerOperationId;

    @Payload
    @Description("物料")
    @ManyToOne(fetch = FetchType.EAGER)
    private MaterialEntity material;

    @Description("装配数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '装配数量'")
    private Double quantity;

}
