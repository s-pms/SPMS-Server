package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>合同附件关联</h1>
 * <p>
 * {@code contract} 与 {@code document} 的中间表
 * </p>
 *
 * @author Hamm.cn
 * @apiNote 同 {@code ContractParticipantLinkEntity}：{@code @ManyToMany} 的中间表由
 * Hibernate 隐式生成，结构不可控也带不了业务字段，改用显式实体
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
    private ContractEntity contract;

    @Description("附件")
    @ManyToOne(fetch = FetchType.EAGER)
    private ContractDocumentEntity document;
}
