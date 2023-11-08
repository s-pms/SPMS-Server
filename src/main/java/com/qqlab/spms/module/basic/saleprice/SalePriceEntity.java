package com.qqlab.spms.module.basic.saleprice;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.basic.customer.CustomerEntity;
import com.qqlab.spms.module.basic.material.MaterialEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * <h1>销售价格实体</h1>
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
@Table(name = "sale_price")
@Description("销售价格")
public class SalePriceEntity extends BaseEntity<SalePriceEntity> {
    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private MaterialEntity material;

    /**
     * <h2>客户信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private CustomerEntity customer;

    @Description("销售单价")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '销售单价'")
    private Double salePrice;
}
