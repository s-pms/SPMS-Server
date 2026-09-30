package cn.hamm.spms.module.asset.contract.participant;

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
 * <h1>合同与参与方的关联实体</h1>
 * <p>
 * 取代原先 {@code ContractEntity.participantList} 上的 {@code @ManyToMany}。
 * {@code @ManyToMany} 的中间表由 Hibernate 隐式生成，结构不可控、也无法携带业务字段；
 * 改为显式的关联实体后，中间表就是一个普通的业务表，可加索引、可加审计列、可被单独查询。
 * </p>
 * <p>
 * 关联本身对前端不可见：{@code ContractEntity.participantList} 已改为
 * {@code @Transient}，由 {@code ContractService} 在读取时组装、写入时同步。
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
@Table(name = "contract_participant_link")
@Description("合同参与方关联")
public class ContractParticipantLinkEntity extends BaseEntity<ContractParticipantLinkEntity> {

    @Description("所属合同")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @Description("参与方")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant_id", nullable = false)
    private ParticipantEntity participant;
}
