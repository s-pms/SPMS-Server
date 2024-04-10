package cn.hamm.spms.module.mes.pickout.detail;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>领料明细实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@Table(name = "pickout_detail")
@Description("领料明细")
public class PickoutDetailEntity extends BaseBillDetailEntity<PickoutDetailEntity> {
    /**
     * 物料信息
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
