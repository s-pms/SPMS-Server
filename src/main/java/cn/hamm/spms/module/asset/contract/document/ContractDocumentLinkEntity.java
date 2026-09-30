package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>合同与附件的关联实体</h1>
 * <p>
 * 取代原先 {@code ContractEntity.documentList} 上的 {@code @ManyToMany}，
 * 原因与 {@code ContractParticipantLinkEntity} 相同。
 * </p>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "contract_document_link")
@Description("合同附件关联")
public class ContractDocumentLinkEntity extends BaseEntity<ContractDocumentLinkEntity> {

    @Description("所属合同")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @Description("附件")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private ContractDocumentEntity document;
}
