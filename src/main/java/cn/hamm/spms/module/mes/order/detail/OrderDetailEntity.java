package cn.hamm.spms.module.mes.order.detail;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>订单明细实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "order_detail")
@Description("订单明细")
public class OrderDetailEntity extends BaseBillDetailEntity<OrderDetailEntity> {
    @Description("数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '数量'")
    private Double quantity;

    @Description("已完成数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已完成数量'")
    private Double finishQuantity;
}
