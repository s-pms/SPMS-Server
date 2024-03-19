package com.qqlab.spms.module.wms.input;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.common.annotation.AutoGenerateCode;
import com.qqlab.spms.module.channel.purchase.PurchaseEntity;
import com.qqlab.spms.module.factory.structure.StructureEntity;
import com.qqlab.spms.module.mes.order.OrderEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import com.qqlab.spms.module.wms.input.detail.InputDetailEntity;
import com.qqlab.spms.module.wms.move.MoveEntity;
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
@Table(name = "input")
@Description("入库单")
public class InputEntity extends AbstractBaseBillEntity<InputEntity, InputDetailEntity> {
    @Description("入库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '入库单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.InputBillCode)
    @Search(Search.Mode.LIKE)
    private String billCode;

    @Description("入库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '入库状态'")
    @Dictionary(value = InputStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer status;

    @Description("入库类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '入库类型'")
    @Dictionary(value = InputType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer type;

    /**
     * 采购信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private PurchaseEntity purchase;

    /**
     * 退料位置
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private StructureEntity structure;

    /**
     * 生产订单
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private OrderEntity order;

    /**
     * 移库单
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private MoveEntity move;
}
