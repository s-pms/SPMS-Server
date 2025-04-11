package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.interfaces.IFile;
import cn.hamm.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>合同附件实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "contract_document")
@Description("合同附件")
public class ContractDocumentEntity extends BaseEntity<ContractDocumentEntity> implements IFile<ContractDocumentEntity> {
    @Description("附件地址")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '附件地址'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "附件地址不能为空")
    private String url;
}