package cn.hamm.spms.module.wms.move;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import cn.hamm.spms.module.wms.move.detail.MoveDetailEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>移库单实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "move")
@Description("移库单")
public class MoveEntity extends AbstractBaseBillEntity<MoveEntity, MoveDetailEntity> {
    @Description("移库单号")
    @Column(columnDefinition = "varchar(255) default '' comment '移库单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.MoveBillCode)
    @Search(Search.Mode.LIKE)
    private String billCode;

    @Description("移库状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '移库状态'")
    @Dictionary(value = MoveStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer status;

    @Description("入库存储资源")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "入库存储资源")
    private StorageEntity storage;
}
