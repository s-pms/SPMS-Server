package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static jakarta.persistence.FetchType.EAGER;

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
public class ContractDocumentEntity extends BaseEntity<ContractDocumentEntity> {
    @Description("合同信息")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "合同信息不能为空")
    private ContractEntity contract;

    @Description("附件地址")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '附件地址'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "附件地址不能为空")
    private String path;
}