package cn.hamm.spms.module.wms.move.detail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.meta.Meta;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
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
 * <h1>移库明细实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "move_detail")
@Description("移库明细")
public class MoveDetailEntity extends BaseBillDetailEntity<MoveDetailEntity> {
    @Description("库存信息")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "库存信息")
    private InventoryEntity inventory;

    @Description("移动数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '移动数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "移动数量不能为空")
    @Meta
    private Double quantity;

    @Description("已移动数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已移动数量'")
    @Meta
    private Double finishQuantity;
}
