package com.qqlab.spms.module.mes.operation;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zfy
 * @date 2023/12/14
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Description("工序")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "operation")
@EqualsAndHashCode(callSuper = true)
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
