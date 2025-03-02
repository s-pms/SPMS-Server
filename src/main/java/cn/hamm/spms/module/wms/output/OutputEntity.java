package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.mes.picking.PickingEntity;
import cn.hamm.spms.module.wms.move.MoveEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
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
import static cn.hamm.spms.module.system.coderule.CodeRuleField.OutputBillCode;
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
@Table(name = "output")
@Description("出库单")
public class OutputEntity extends AbstractBaseBillEntity<OutputEntity, OutputDetailEntity> {
    @Description("出库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '出库单号'", unique = true)
    @AutoGenerateCode(OutputBillCode)
    @Search
    private String billCode;

    @Description("出库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '出库状态'")
    @Dictionary(value = OutputStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer status;

    @Description("出库类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '出库类型'")
    @Dictionary(value = OutputType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer type;

    @Description("销售单")
    @ManyToOne(fetch = EAGER)
    private SaleEntity sale;

    @Description("移库单")
    @ManyToOne(fetch = EAGER)
    private MoveEntity move;

    @Description("领料单")
    @ManyToOne(fetch = EAGER)
    private PickingEntity picking;
}
