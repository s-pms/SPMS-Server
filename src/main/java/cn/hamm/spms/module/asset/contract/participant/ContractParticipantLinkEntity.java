package cn.hamm.spms.module.asset.contract.participant;

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
 * <h1>合同参与方关联</h1>
 * <p>
 * {@code contract} 与 {@code participant} 的中间表
 * </p>
 *
 * @author Hamm.cn
 * @apiNote 取代原先 {@code ContractEntity.participantList} 上的 {@code @ManyToMany}：
 * 中间表由 Hibernate 隐式生成则结构不可控也带不了业务字段，显式实体就是一张普通业务表。
 * 关联本身对前端不可见，{@code ContractEntity.participantList} 是 {@code @Transient}，
 * 由 {@code ContractService} 读取时组装、写入时同步
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "contract_participant_link")
@Description("合同参与方关联")
public class ContractParticipantLinkEntity extends BaseEntity<ContractParticipantLinkEntity> {
    @Description("所属合同")
    @ManyToOne(fetch = FetchType.EAGER)
    private ContractEntity contract;

    @Description("参与方")
    @ManyToOne(fetch = FetchType.EAGER)
    private ParticipantEntity participant;
}
