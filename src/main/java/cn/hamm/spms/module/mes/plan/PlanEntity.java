package cn.hamm.spms.module.mes.plan;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;
import static cn.hamm.spms.module.system.coderule.CodeRuleField.PlanBillCode;
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
@Table(name = "plan")
@Description("生产计划")
public class PlanEntity extends AbstractBaseBillEntity<PlanEntity, PlanDetailEntity> {
    @Description("生产计划号")
    @Column(columnDefinition = "varchar(255) default '' comment '生产计划号'", unique = true)
    @AutoGenerateCode(PlanBillCode)
    @Search
    private String billCode;

    @Description("计划状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '计划状态'")
    @Dictionary(value = PlanStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer status;

    @Description("计划类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '计划类型'")
    @Dictionary(value = PlanType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer type;

    @Description("交付时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '交付时间'")
    private Long deliverTime;

    @Description("开始时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '开始时间'")
    private Long startTime;

    @Description("完成时间")
    @ReadOnly
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '完成时间'")
    private Long finishTime;

    @Description("客户信息")
    @ManyToOne(fetch = EAGER)
    private CustomerEntity customer;
}
