package com.qqlab.spms.module.wms.input;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.module.channel.purchase.PurchaseEntity;
import com.qqlab.spms.module.factory.storage.StorageEntity;
import com.qqlab.spms.module.factory.structure.StructureEntity;
import com.qqlab.spms.module.mes.order.OrderEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import com.qqlab.spms.module.wms.input.detail.InputDetailEntity;
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
@Table(name = "input")
@Description("入库单")
public class InputEntity extends AbstractBaseBillEntity<InputEntity, InputDetailEntity> {
    @Description("入库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '入库单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.InputBillCode)
    private String billCode;

    @Description("入库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '入库状态'")
    @Dictionary(InputStatus.class)
    private Integer status;

    @Description("入库类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '入库类型'")
    @Dictionary(InputType.class)
    private Integer type;

    /**
     * <h2>入库存储资源</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "入库存储资源")
    private StorageEntity storage;

    /**
     * <h2>开始时间</h2>
     */
    @Description("开始时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '开始时间'")
    private Long startTime;

    /**
     * <h2>采购信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private PurchaseEntity purchase;

    /**
     * <h2>退料位置</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private StructureEntity structure;

    /**
     * <h2>生产订单</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private OrderEntity order;
}
