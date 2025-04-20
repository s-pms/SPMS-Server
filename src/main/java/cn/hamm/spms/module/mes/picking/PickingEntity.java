package cn.hamm.spms.module.mes.picking;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import cn.hamm.spms.module.mes.order.OrderEntity;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailEntity;
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

import static cn.hamm.spms.module.system.coderule.CodeRuleField.PickingBillCode;
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
@Table(name = "picking")
@Description("领料单")
public class PickingEntity extends AbstractBaseBillEntity<PickingEntity, PickingDetailEntity> {
    @Description("领料单号")
    @Column(columnDefinition = "varchar(255) default '' comment '领料单号'", unique = true)
    @AutoGenerateCode(PickingBillCode)
    private String billCode;

    @Description("领料状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '领料状态'")
    @Dictionary(value = PickingStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @ReadOnly
    private Integer status;

    @Description("领料类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '领料类型'")
    @Dictionary(value = PickingType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("领料位置")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "领料位置不能为空")
    private StructureEntity structure;

    @Description("关联订单")
    @ManyToOne(fetch = EAGER)
    private OrderEntity order;

    // todo 如果是工序级别的BOM 则需要关联生产时的工单
}
