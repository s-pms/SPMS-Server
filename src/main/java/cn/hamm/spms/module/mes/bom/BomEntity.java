package cn.hamm.spms.module.mes.bom;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;
import static cn.hamm.spms.module.system.coderule.CodeRuleField.BomCode;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.FetchType.EAGER;

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
public class BomEntity extends BaseEntity<BomEntity> {
    @Description("配方编码")
    @Column(columnDefinition = "varchar(255) default '' comment '配方编码'", unique = true)
    @AutoGenerateCode(BomCode)
    @Search
    private String code;

    @Description("配方名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '配方名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "配方名称不能为空")
    private String name;

    @Description("配方状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '配方状态'")
    @Dictionary(value = BomStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    @ReadOnly
    private Integer status;

    @Description("配方类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '配方类型'")
    @Dictionary(value = BomType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer type;

    @Description("配方明细")
    @ManyToMany(fetch = EAGER, cascade = PERSIST)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "配方明细不能为空")
    private Set<BomDetailEntity> details;
}
