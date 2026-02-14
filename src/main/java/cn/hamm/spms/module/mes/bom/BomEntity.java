package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.annotation.ReadOnly;
import cn.hamm.airpower.curd.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import cn.hamm.spms.module.mes.bom.enums.BomStatus;
import cn.hamm.spms.module.mes.bom.enums.BomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;

import static cn.hamm.spms.module.system.coderule.enums.CodeRuleField.BomCode;

/**
 * <h1>BOM 实体</h1>
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
public class BomEntity extends BaseEntity<BomEntity> {
    @Description("配方编码")
    @Column(columnDefinition = "varchar(255) default '' comment '配方编码'", unique = true)
    @AutoGenerateCode(BomCode)
    @Search
    @Meta
    private String code;

    @Description("配方名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '配方名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "配方名称不能为空")
    @Meta
    private String name;

    @Description("配方状态")
    @Column(columnDefinition = "int UNSIGNED default 1 comment '配方状态'")
    @Dictionary(value = BomStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @ReadOnly
    private Integer status;

    @Description("配方类型")
    @Column(columnDefinition = "int UNSIGNED default 1 comment '配方类型'")
    @Dictionary(value = BomType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("配方明细")
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "配方明细不能为空")
    private Set<BomDetailEntity> details;
}
