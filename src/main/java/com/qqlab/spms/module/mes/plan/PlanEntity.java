package com.qqlab.spms.module.mes.plan;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.common.annotation.AutoGenerateCode;
import com.qqlab.spms.module.channel.customer.CustomerEntity;
import com.qqlab.spms.module.mes.plan.detail.PlanDetailEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.*;
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
@Table(name = "plan")
@Description("生产计划")
public class PlanEntity extends AbstractBaseBillEntity<PlanEntity, PlanDetailEntity> {
    @Description("生产计划号")
    @Column(columnDefinition = "varchar(255) default '' comment '生产计划号'", unique = true)
    @AutoGenerateCode(CodeRuleField.PlanBillCode)
    private String billCode;

    @Description("计划状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '计划状态'")
    @Dictionary(value = PlanStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer status;

    @Description("计划类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '计划类型'")
    @Dictionary(value = PlanType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    /**
     * 交付时间
     */
    @Description("交付时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '交付时间'")
    private Long deliverTime;

    /**
     * 开始时间
     */
    @Description("开始时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '开始时间'")
    private Long startTime;

    /**
     * 完成时间
     */
    @Description("完成时间")
    @ReadOnly
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '完成时间'")
    private Long finishTime;

    /**
     * 客户信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private CustomerEntity customer;
}
