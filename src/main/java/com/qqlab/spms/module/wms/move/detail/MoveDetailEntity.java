package com.qqlab.spms.module.wms.move.detail;


import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
import com.qqlab.spms.module.wms.inventory.InventoryEntity;
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
 * <h1>移库明细实体</h1>
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
@Table(name = "move_detail")
@Description("移库明细")
public class MoveDetailEntity extends BaseBillDetailEntity<MoveDetailEntity> {
    /**
     * <h2>库存信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "库存信息")
    private InventoryEntity inventory;

    @Description("移动数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '移动数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "移动数量不能为空")
    private Double quantity;

    @Description("已移动数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已移动数量'")
    private Double finishQuantity;
}
