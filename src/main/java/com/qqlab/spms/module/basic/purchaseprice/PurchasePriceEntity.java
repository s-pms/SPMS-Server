package com.qqlab.spms.module.basic.purchaseprice;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.basic.material.MaterialEntity;
import com.qqlab.spms.module.basic.supplier.SupplierEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

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
    private MaterialEntity material;

    /**
     * <h2>供应商信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private SupplierEntity supplier;

    @Description("采购价")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '采购价'")
    private Double purchasePrice;
}
