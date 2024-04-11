package cn.hamm.spms.module.mes.operation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zfy
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "operation")
@Description("工序")
public class OperationEntity extends BaseEntity<OperationEntity> {
    @Description("工序名称")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工序名称不能为空")
    @Column(columnDefinition = "varchar(255) default '' comment '工序名称'", unique = true)
    private String name;

    @Description("工序编码")
    @Column(columnDefinition = "varchar(255) default '' comment '工序编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.OperationCode)
    private String code;

}
