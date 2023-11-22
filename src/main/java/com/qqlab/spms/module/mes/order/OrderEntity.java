package com.qqlab.spms.module.mes.order;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.channel.customer.CustomerEntity;
import com.qqlab.spms.module.mes.order.detail.OrderDetailEntity;
import com.qqlab.spms.module.mes.plan.PlanEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
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
 * <h1>领料单实体</h1>
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
@Table(name = "orders")
@Description("生产订单")
public class OrderEntity extends AbstractBaseBillEntity<OrderEntity, OrderDetailEntity> {
    @Description("订单号")
    @Column(columnDefinition = "varchar(255) default '' comment '订单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.OrderBillCode)
    private String billCode;

    @Description("订单状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '订单状态'")
    @Dictionary(OrderStatus.class)
    private Integer status;

    @Description("订单类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '订单类型'")
    @Dictionary(OrderType.class)
    private Integer type;

    /**
     * <h2>开始时间</h2>
     */
    @Description("开始时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '开始时间'")
    private Long startTime;

    /**
     * <h2>完成时间</h2>
     */
    @Description("完成时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '完成时间'")
    private Long finishTime;

    /**
     * <h2>交付时间</h2>
     */
    @Description("交付时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '交付时间'")
    private Long deliverTime;

    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("生产数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '生产数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "生产数量不能为空")
    private Double quantity;

    @Description("已完成数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已完成数量'")
    private Double finishQuantity;

    /**
     * <h2>计划信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private PlanEntity plan;

    /**
     * <h2>客户信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private CustomerEntity customer;
}
