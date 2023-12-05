package com.qqlab.spms.module.wms.output;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.module.channel.sale.SaleEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import com.qqlab.spms.module.wms.move.MoveEntity;
import com.qqlab.spms.module.wms.output.detail.OutputDetailEntity;
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
@Table(name = "output")
@Description("出库单")
public class OutputEntity extends AbstractBaseBillEntity<OutputEntity, OutputDetailEntity> {
    @Description("出库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '出库单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.OutputBillCode)
    @Search(Search.Mode.LIKE)
    private String billCode;

    @Description("出库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '出库状态'")
    @Dictionary(OutputStatus.class)
    @Search(Search.Mode.EQUALS)
    private Integer status;

    @Description("出库类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '出库类型'")
    @Dictionary(OutputType.class)
    @Search(Search.Mode.EQUALS)
    private Integer type;

    /**
     * 销售单
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private SaleEntity sale;

    /**
     * 移库单
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private MoveEntity move;
}
