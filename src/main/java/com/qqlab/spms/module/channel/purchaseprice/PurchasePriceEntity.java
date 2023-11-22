package com.qqlab.spms.module.channel.purchaseprice;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.channel.supplier.SupplierEntity;
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
 * <h1>采购价格实体</h1>
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
@Table(name = "purchase_price")
@Description("采购价格")
public class PurchasePriceEntity extends BaseEntity<PurchasePriceEntity> {
    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class, WhenGetByMaterialAndSupplier.class}, message = "物料不能为空")
    private MaterialEntity material;

    /**
     * <h2>供应商信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class, WhenGetByMaterialAndSupplier.class}, message = "供应商不能为空")
    private SupplierEntity supplier;

    @Description("采购单价")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '采购单价'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "采购单价不能为空")
    private Double price;

    public interface WhenGetByMaterialAndSupplier {
    }
}
