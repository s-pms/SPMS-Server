package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
     * 把合同的参与方同步到中间表（全量覆盖）
     * <p>
     * 采用「先删后建」而非增量 diff：合同的参与方数量有限（个位数），
     * 全量覆盖实现简单且不会漏删；关联实体自带主键，也便于追溯历史。
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
        // 先清掉旧关联。
        // 必须用 deleteAll + flush，不能写成 forEach(this::delete)：
        // CurdService.delete(long) 走的是 TransactionHelper.run(Function)，
        // 而该重载没有 @Transactional（只有 run(Supplier) 有），循环里下一条的
        // get(id) 查询会触发 auto-flush，把上一条刷进库，
        // **最后一条的删除标记永远丢失**。曾因此漏删关联行。
        List<ContractParticipantLinkEntity> exists = filter(
                new ContractParticipantLinkEntity().setContract(new ContractEntity().setId(contractId)));
        if (!exists.isEmpty()) {
            repository.deleteAll(exists);
            repository.flush();
        }

        if (participants.isEmpty()) {
            return;
        }
        var participantService = cn.hamm.spms.module.asset.AssetServices.getParticipantService();
        ContractEntity contract = new ContractEntity().setId(contractId);
        for (ParticipantEntity participant : participants) {
            if (Objects.isNull(participant)) {
                continue;
            }
            // 没有 ID 说明是本次新增的，先落库拿到 ID
            if (Objects.isNull(participant.getId())) {
                participant = participantService.addAndGet(participant);
            }
            addAndGet(new ContractParticipantLinkEntity()
                    .setContract(contract)
                    .setParticipant(participant));
        }
    }
}
