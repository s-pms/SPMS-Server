package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>合同参与方关联</h1>
 * <p>
 * 承载 {@code contract <-> participant} 多对多关系
 * </p>
 *
 * @author Hamm.cn
 * @apiNote 不用 {@code @ManyToMany}：其中间表由 Hibernate 隐式生成，结构不可控也带不了业务字段；
 * 显式关联实体就是一张普通业务表
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
     * 把合同的参与方同步到中间表（增量）
     *
     * @param contractId   合同 ID
     * @param participants 前端提交的参与方
     * @apiNote 只解绑本次提交里已不存在的、只建立本次新增的；没有 ID 的参与方先落库拿到 ID 再建关联
     */
    public void syncByContractId(long contractId, @NotNull Collection<ParticipantEntity> participants) {
        var participantService = cn.hamm.spms.module.asset.AssetServices.getParticipantService();
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
        // 走 service.delete 保证前后置钩子被触发
        stale.forEach(entity -> delete(entity.getId()));
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
