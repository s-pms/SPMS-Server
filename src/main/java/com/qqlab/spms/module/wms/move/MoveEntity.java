package com.qqlab.spms.module.wms.move;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.common.annotation.AutoGenerateCode;
import com.qqlab.spms.module.factory.storage.StorageEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import com.qqlab.spms.module.wms.move.detail.MoveDetailEntity;
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
 * <h1>移库单实体</h1>
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
@Table(name = "move")
@Description("移库单")
public class MoveEntity extends AbstractBaseBillEntity<MoveEntity, MoveDetailEntity> {
    @Description("移库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '移库单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.MoveBillCode)
    private String billCode;

    @Description("移库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '移库状态'")
    @Dictionary(MoveStatus.class)
    private Integer status;

    /**
     * 入库存储资源
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "入库存储资源")
    private StorageEntity storage;
}
