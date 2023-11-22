package com.qqlab.spms.module.mes.pickout.detail;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
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
 * <h1>领料明细实体</h1>
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
@Table(name = "pickout_detail")
@Description("领料明细")
public class PickoutDetailEntity extends BaseBillDetailEntity<PickoutDetailEntity> {
    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("领料数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '领料数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "领料数量不能为空")
    private Double quantity;

    @Description("已出库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已出库数量'")
    private Double finishQuantity;
}
