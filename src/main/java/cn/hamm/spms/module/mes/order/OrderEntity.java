package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.util.Meta;
import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.util.annotation.ReadOnly;
import cn.hamm.airpower.web.annotation.Search;
import cn.hamm.airpower.web.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.enums.OrderStatus;
import cn.hamm.spms.module.mes.order.enums.OrderType;
import cn.hamm.spms.module.mes.plan.PlanEntity;
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

import static cn.hamm.spms.module.system.coderule.enums.CodeRuleField.OrderBillCode;
import static jakarta.persistence.FetchType.EAGER;

/**
 * <h1>领料单实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "orders")
@Description("生产订单")
public class OrderEntity extends AbstractBaseBillEntity<OrderEntity, OrderDetailEntity> {
    @Description("订单号")
    @Column(columnDefinition = "varchar(255) default '' comment '订单号'", unique = true)
    @AutoGenerateCode(OrderBillCode)
    @Search
    @Meta
    private String billCode;

    @Description("订单状态")
    @Column(columnDefinition = "int UNSIGNED default 1 comment '订单状态'")
    @Dictionary(value = OrderStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer status;

    @Description("订单类型")
    @Column(columnDefinition = "int UNSIGNED default 1 comment '订单类型'")
    @Dictionary(value = OrderType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("开始时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '开始时间'")
    private Long startTime;

    @Description("完成时间")
    @ReadOnly
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '完成时间'")
    private Long finishTime;

    @Description("交付时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '交付时间'")
    private Long deliverTime;

    @Description("物料信息")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("生产数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '生产数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "生产数量不能为空")
    private Double quantity;

    @Description("已完成数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已完成数量'")
    @ReadOnly
    private Double finishQuantity;

    @Description("异常数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '异常数量'")
    private Double ngQuantity;

    @Description("计划信息")
    @ManyToOne(fetch = EAGER)
    private PlanEntity plan;

    @Description("客户信息")
    @ManyToOne(fetch = EAGER)
    private CustomerEntity customer;
}
