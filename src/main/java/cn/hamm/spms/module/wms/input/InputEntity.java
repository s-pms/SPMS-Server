package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.channel.purchase.PurchaseEntity;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import cn.hamm.spms.module.mes.order.OrderEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.move.MoveEntity;
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
import static cn.hamm.airpower.annotation.Search.Mode.LIKE;
import static cn.hamm.spms.module.system.coderule.CodeRuleField.InputBillCode;
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
@Table(name = "input")
@Description("入库单")
public class InputEntity extends AbstractBaseBillEntity<InputEntity, InputDetailEntity> {
    @Description("入库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '入库单号'", unique = true)
    @AutoGenerateCode(InputBillCode)
    @Search(LIKE)
    private String billCode;

    @Description("入库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '入库状态'")
    @Dictionary(value = InputStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer status;

    @Description("入库类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '入库类型'")
    @Dictionary(value = InputType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer type;

    @Description("采购信息")
    @ManyToOne(fetch = EAGER)
    private PurchaseEntity purchase;

    @Description("退料位置")
    @ManyToOne(fetch = EAGER)
    private StructureEntity structure;

    @Description("生产订单")
    @ManyToOne(fetch = EAGER)
    private OrderEntity order;

    @Description("移库单")
    @ManyToOne(fetch = EAGER)
    private MoveEntity move;
}
