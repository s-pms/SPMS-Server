package com.qqlab.spms.module.channel.purchase.detail;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.channel.supplier.SupplierEntity;
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
 * <h1>采购明细实体</h1>
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
@Table(name = "purchase_detail")
@Description("采购明细")
public class PurchaseDetailEntity extends BaseBillDetailEntity<PurchaseDetailEntity> {
    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    /**
     * <h2>供应商信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "供应商不能为空")
    private SupplierEntity supplier;

    @Description("采购数量")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '采购数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "采购数量不能为空")
    private Double quantity;

    @Description("采购单价")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '采购单价'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "采购单价不能为空")
    private Double price;

    @Description("已入库数量")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '已入库数量'")
    private Double inputQuantity;
}