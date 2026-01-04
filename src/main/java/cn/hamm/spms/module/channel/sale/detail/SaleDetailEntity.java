package cn.hamm.spms.module.channel.sale.detail;

import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.ReadOnly;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static jakarta.persistence.FetchType.EAGER;

/**
 * <h1>销售明细实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "sale_detail")
@Description("销售明细")
public class SaleDetailEntity extends BaseBillDetailEntity<SaleDetailEntity> {
    @Description("物料信息")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("销售数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '销售数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "销售数量不能为空")
    @Meta
    private Double quantity;

    @Description("销售单价")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '销售单价'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "销售单价不能为空")
    @Meta
    private Double price;

    @Description("已出库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已出库数量'")
    @Meta
    @ReadOnly
    private Double finishQuantity;
}
