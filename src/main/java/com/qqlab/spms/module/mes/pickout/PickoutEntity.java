package com.qqlab.spms.module.mes.pickout;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.module.factory.structure.StructureEntity;
import com.qqlab.spms.module.mes.pickout.detail.PickoutDetailEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>领料单实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "pickout")
@Description("领料单")
public class PickoutEntity extends AbstractBaseBillEntity<PickoutEntity, PickoutDetailEntity> {
    @Description("领料单号")
    @Column(columnDefinition = "varchar(255) default '' comment '领料单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.PickoutBillCode)
    private String billCode;

    @Description("领料状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '领料状态'")
    @Dictionary(PickoutStatus.class)
    private Integer status;

    /**
     * <h2>领料位置</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "领料位置不能为空")
    private StructureEntity structure;
}
