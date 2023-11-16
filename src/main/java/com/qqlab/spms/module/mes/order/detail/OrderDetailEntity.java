package com.qqlab.spms.module.mes.order.detail;


import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * <h1>订单明细实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "order_detail")
@Description("订单明细")
public class OrderDetailEntity extends BaseBillDetailEntity<OrderDetailEntity> {
}
