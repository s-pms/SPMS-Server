package com.qqlab.spms.module.mes.plan;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.bill.BaseBillEntity;
import com.qqlab.spms.module.channel.customer.CustomerEntity;
import com.qqlab.spms.module.mes.plan.detail.PlanDetailEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

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
@Table(name = "plan")
@Description("生产计划")
public class PlanEntity extends BaseBillEntity<PlanEntity, PlanDetailEntity> {
    @Description("生产计划号")
    @Column(columnDefinition = "varchar(255) default '' comment '生产计划号'", unique = true)
    @AutoGenerateCode(CodeRuleField.PlanBillCode)
    private String billCode;

    @Description("计划状态")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '计划状态'")
    @Dictionary(PlanStatus.class)
    private Integer status;

    @Description("计划类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '计划类型'")
    @Dictionary(PlanType.class)
    private Integer type;

    /**
     * <h2>交付时间</h2>
     */
    @Description("交付时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '交付时间'")
    private Long deliverTime;

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
     * <h2>客户信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private CustomerEntity customer;
}
