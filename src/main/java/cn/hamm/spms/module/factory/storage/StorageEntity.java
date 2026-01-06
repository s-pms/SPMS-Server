package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.interfaces.ITree;
import cn.hamm.airpower.web.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import java.util.List;

import static cn.hamm.spms.module.system.coderule.enums.CodeRuleField.StorageCode;

/**
 * <h1>仓库实体</h1>
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
@Description("仓库")
public class StorageEntity extends BaseEntity<StorageEntity> implements ITree<StorageEntity> {
    @Description("仓库编码")
    @Column(columnDefinition = "varchar(255) default '' comment '仓库编码'", unique = true)
    @AutoGenerateCode(StorageCode)
    @Meta
    private String code;

    @Description("名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '名称'")
    @Length(max = 200, message = "名称最多允许{max}个字符")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "名称不能为空")
    @Meta
    private String name;

    @Description("父级 ID")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '父级ID'")
    @Meta
    private Long parentId;

    @Description("树子集节点数组")
    @Transient
    private List<StorageEntity> children;
}
