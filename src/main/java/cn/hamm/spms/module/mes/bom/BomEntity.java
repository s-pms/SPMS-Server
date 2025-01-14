package cn.hamm.spms.module.mes.bom;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>BOM实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "bom")
@Description("BOM")
public class BomEntity extends AbstractBaseBillEntity<BomEntity, BomDetailEntity> {
    @Description("配方编码")
    @Column(columnDefinition = "varchar(255) default '' comment '配方编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.BOM_CODE)
    @Search(Search.Mode.LIKE)
    private String billCode;

    @Description("配方名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '配方名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "配方名称不能为空")
    private String name;

    @Description("配方状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '配方状态'")
    @Dictionary(value = BomStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    @ReadOnly
    private Integer status;

    @Description("配方类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '配方类型'")
    @Dictionary(value = BomType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer type;
}
