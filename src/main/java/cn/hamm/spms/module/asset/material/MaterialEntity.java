package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import cn.hamm.spms.module.system.unit.UnitEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;
import static cn.hamm.airpower.annotation.Search.Mode.JOIN;

/**
 * <h1>物料实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "material")
@Description("物料")
public class MaterialEntity extends BaseEntity<MaterialEntity> {
    @Description("物料名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '物料名称'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "物料名称不能为空")
    private String name;

    @Description("物料编码")
    @Column(columnDefinition = "varchar(255) default '' comment '物料编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.MaterialCode)
    private String code;

    @Description("规格型号")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '规格型号'")
    private String spc;

    @Description("物料类型")
    @Search(EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 1 comment '物料类型'")
    @Dictionary(value = MaterialType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer materialType;

    @Description("使用方式")
    @Search(EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 1 comment '使用方式'")
    @Dictionary(value = MaterialUseType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer useType;

    @Description("默认单位")
    @ManyToOne
    @Search(JOIN)
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "默认单位不能为空")
    private UnitEntity unitInfo;

    @Description("采购标准价")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '采购标准价'")
    private Double purchasePrice;

    @Description("销售标准价")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '销售标准价'")
    private Double salePrice;
}
