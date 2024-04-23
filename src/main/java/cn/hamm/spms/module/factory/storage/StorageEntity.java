package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseTreeEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>存储资源实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "storage")
@Description("存储资源")
public class StorageEntity extends BaseTreeEntity<StorageEntity> {
    @Description("存储资源编码")
    @Column(columnDefinition = "varchar(255) default '' comment '存储资源编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.StorageCode)
    private String code;
}
