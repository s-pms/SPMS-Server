package com.qqlab.spms.module.channel.sale.detail;


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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * <h1>销售明细实体</h1>
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
@Table(name = "sale_detail")
@Description("销售明细")
public class SaleDetailEntity extends BaseBillDetailEntity<SaleDetailEntity> {
    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("销售数量")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '销售数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "销售数量不能为空")
    private Double quantity;

    @Description("销售单价")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '销售单价'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "销售单价不能为空")
    private Double price;

    @Description("已出库数量")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '已出库数量'")
    private Double outputQuantity;

//    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '单据号'")
//    private Long billId;
}
