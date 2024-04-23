package cn.hamm.spms.module.wms.output.detail;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>出库明细实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "output_detail")
@Description("出库明细")
public class OutputDetailEntity extends BaseBillDetailEntity<OutputDetailEntity> {
    @Description("库存信息")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "库存信息")
    private InventoryEntity inventory;

    @Description("物料信息")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料信息")
    private MaterialEntity material;

    @Description("出库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '出库数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "出库数量不能为空")
    private Double quantity;

    @Description("已出库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已出库数量'")
    private Double finishQuantity;
}
