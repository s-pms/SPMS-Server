package com.qqlab.spms.module.mes.plan.detail;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * <h1>计划明细实体</h1>
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
@Table(name = "plan_detail")
@Description("计划明细")
public class PlanDetailEntity extends BaseBillDetailEntity<PlanDetailEntity> {
    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("生产数量")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '生产数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "生产数量不能为空")
    private Double quantity;

    @Description("已完成数量")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '已完成数量'")
    private Double finishQuantity;
}
