package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import cn.hamm.spms.module.system.unit.UnitEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;



/**
 * <h1>物料实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@Table(name = "material")
@Description("物料")
public class MaterialEntity extends BaseEntity<MaterialEntity> {
    /**
     * 物料名称
     */
    @Description("物料名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '物料名称'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "物料名称不能为空")
    private String name;

    @Description("物料编码")
    @Column(columnDefinition = "varchar(255) default '' comment '物料编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.MaterialCode)
    private String code;

    /**
     * 规格型号
     */
    @Description("规格型号")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '规格型号'")
    private String spc;

    /**
     * 物料类型
     */
    @Description("物料类型")
    @Search(Search.Mode.EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '物料类型'")
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "物料类型不能为空")
    @Dictionary(value = MaterialType.class,groups = {WhenAdd.class, WhenUpdate.class})
    private Integer materialType;

    /**
     * 默认单位
     */
    @ManyToOne
    @Search(Search.Mode.JOIN)
    @JoinColumn(nullable = false, name = "unit")
    @Payload
    private UnitEntity unitInfo;

    @Description("采购标准价")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '采购标准价'")
    private Double purchasePrice;

    @Description("采购标准价")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '采购标准价'")
    private Double salePrice;
}
