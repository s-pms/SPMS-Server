package cn.hamm.spms.module.mes.order.detail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.meta.Meta;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    @Meta
    private Double quantity;

    @Description("完成数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '完成数量'")
    @NotNull(groups = {WhenAddFinish.class}, message = "完成数量不能为空")
    @Meta
    private Double finishQuantity;

    @Description("异常数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '异常数量'")
    @Meta
    private Double ngQuantity;
}
