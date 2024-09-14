package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import cn.hamm.spms.module.wms.move.MoveEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
@Table(name = "output")
@Description("出库单")
public class OutputEntity extends AbstractBaseBillEntity<OutputDetailEntity> {
    @Description("出库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '出库单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.OutputBillCode)
    @Search(Search.Mode.LIKE)
    private String billCode;

    @Description("出库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '出库状态'")
    @Dictionary(value = OutputStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer status;

    @Description("出库类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '出库类型'")
    @Dictionary(value = OutputType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer type;

    @Description("销售单")
    @ManyToOne(fetch = FetchType.EAGER)
    private SaleEntity sale;

    @Description("移库单")
    @ManyToOne(fetch = FetchType.EAGER)
    private MoveEntity move;
}
