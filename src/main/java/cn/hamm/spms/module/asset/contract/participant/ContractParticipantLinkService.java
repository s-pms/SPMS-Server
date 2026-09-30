package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h1>合同参与方关联服务</h1>
 * <p>
 * 承载 {@code contract <-> participant} 的多对多关系。
 * 之所以不用 {@code @ManyToMany}：它的中间表由 Hibernate 隐式生成，
 * 结构不可控、也无法携带业务字段；显式关联实体则是一个普通业务表。
 * </p>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class ContractParticipantLinkService extends BaseService<ContractParticipantLinkEntity, ContractParticipantLinkRepository> {

    /**
     * 查询某个合同的参与方
     *
     * @param contractId 合同 ID
     * @return 参与方集合
     */
    public @NotNull Set<ParticipantEntity> getParticipants(long contractId) {
        List<ContractParticipantLinkEntity> links = filter(
                new ContractParticipantLinkEntity().setContract(new ContractEntity().setId(contractId)));
        return links.stream()
                .map(ContractParticipantLinkEntity::getParticipant)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 把合同的参与方同步到中间表（增量同步）
     * <p>
     * 只解绑「本次提交里已不存在」的参与方、只建立「本次新增」的关联，
     * 不动没有变化的那些行。
     * </p>
     * <p>
     * 不使用 {@code repository.deleteAll} + {@code flush} 这类语句：
     * 它们直接拼批量 SQL，<b>不触发</b> JPA 实体生命周期回调、<b>不走</b>
     * {@code beforeAppDelete} 之类的业务钩子，也不做级联处理 ——
     * 将来给关联实体挂上需要清理的关联时，会静默留下脏数据。
     * 删除一律走框架标准的 {@code delete(id)}。
     * </p>
     * <p>
     * 提交的参与方若没有 ID（前端新增的行），先落库拿到 ID 再建关联，
     * 以此保留原先 {@code cascade = CascadeType.PERSIST} 的语义。
     * </p>
     *
     * @param contractId   合同 ID
     * @param participants 前端提交的参与方
     */
    public void syncByContractId(long contractId, @NotNull Collection<ParticipantEntity> participants) {
        var participantService = cn.hamm.spms.module.asset.AssetServices.getParticipantService();
        // 本次要保留的关联：没有 ID 的先落库拿到 ID
        List<ParticipantEntity> targets = new ArrayList<>();
        for (ParticipantEntity participant : participants) {
            if (Objects.isNull(participant)) {
                continue;
            }
            if (Objects.isNull(participant.getId())) {
                participant = participantService.addAndGet(participant);
            }
            targets.add(participant);
        }
        Set<Long> targetIds = targets.stream()
                .map(ParticipantEntity::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<ContractParticipantLinkEntity> exists = filter(
                new ContractParticipantLinkEntity().setContract(new ContractEntity().setId(contractId)));
        ContractEntity contract = new ContractEntity().setId(contractId);
        Set<Long> kept = new LinkedHashSet<>();
        List<ContractParticipantLinkEntity> stale = new ArrayList<>();
        for (ContractParticipantLinkEntity link : exists) {
            Long participantId = Objects.isNull(link.getParticipant()) ? null : link.getParticipant().getId();
            if (Objects.isNull(participantId) || !targetIds.contains(participantId)) {
                stale.add(link);
            } else {
                kept.add(participantId);
            }
        }
        deleteAll(stale);
        for (ParticipantEntity participant : targets) {
            if (kept.contains(participant.getId())) {
                continue;
            }
            addAndGet(new ContractParticipantLinkEntity()
                    .setContract(contract)
                    .setParticipant(participant));
        }
        log.info("合同 {} 参与方同步完成：原有 {} 个，现 {} 个", contractId, exists.size(), targetIds.size());
    }
}
