package cn.hamm.spms.module.system.unit;

import cn.hamm.airpower.util.Meta;
import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.web.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.spms.module.system.coderule.enums.CodeRuleField.UnitCode;

/**
 * <h1>单位实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "unit")
@Description("单位")
public class UnitEntity extends BaseEntity<UnitEntity> {
    @Description("单位名称")
    @Column(columnDefinition = "varchar(255) default '' comment '单位名称'", unique = true)
    @Search
    @Meta
    private String name;

    @Description("单位编码")
    @Column(columnDefinition = "varchar(255) default '' comment '单位编码'", unique = true)
    @AutoGenerateCode(UnitCode)
    @Meta
    private String code;
}
