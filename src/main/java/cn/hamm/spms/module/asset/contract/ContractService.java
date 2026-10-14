package cn.hamm.spms.module.asset.contract;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.AssetServices;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;

/**
 * <h1>合同</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ContractService extends BaseService<ContractEntity, ContractRepository> {
    @Override
    protected @NotNull ContractEntity beforeAppAdd(@NotNull ContractEntity source) {
        source.setStatus(ContractStatus.INVALID.getKey());
        return source;
    }

    @Override
    protected @NotNull ContractEntity beforeAppUpdate(@NotNull ContractEntity source) {
        ContractEntity exist = get(source.getId());
        source.setStatus(exist.getStatus());
        return source;
    }

    @Override
    protected @NotNull ContractEntity afterAppGet(@NotNull ContractEntity contract) {
        fillLinks(contract);
        return contract;
    }

    @Override
    protected @NotNull List<ContractEntity> afterGetList(@NotNull List<ContractEntity> list) {
        list.forEach(this::fillLinks);
        return list;
    }

    /**
     * 填充参与方与附件
     *
     * @param contract 合同
     */
    private void fillLinks(@NotNull ContractEntity contract) {
        contract.setParticipantList(
                AssetServices.getContractParticipantLinkService().getParticipants(contract.getId()));
        contract.setDocumentList(
                AssetServices.getContractDocumentLinkService().getDocuments(contract.getId()));
    }

    @Override
    protected void afterAppAdd(long id, @NotNull ContractEntity source) {
        syncLinks(id, source);
    }

    @Override
    protected void afterAppUpdate(long id, @NotNull ContractEntity source) {
        syncLinks(id, source);
    }

    /**
     * 把参与方与附件同步到中间表
     *
     * @param contractId 合同 ID
     * @param source     客户端提交的合同
     * @apiNote 必须判空：前端编辑合同基本信息时通常不传这两个集合，
     * 不区分「未传」与「传空集」会导致一次普通修改就把参与方/附件全清空
     */
    private void syncLinks(long contractId, @NotNull ContractEntity source) {
        if (Objects.nonNull(source.getParticipantList())) {
            AssetServices.getContractParticipantLinkService()
                    .syncByContractId(contractId, source.getParticipantList());
        }
        if (Objects.nonNull(source.getDocumentList())) {
            AssetServices.getContractDocumentLinkService()
                    .syncByContractId(contractId, source.getDocumentList());
        }
    }

    /**
     * 生效合同
     *
     * @param id 合同 ID
     */
    public void enforce(long id) {
        ContractEntity exist = get(id);
        FORBIDDEN.when(ContractStatus.INVALID.notEqualsKey(exist.getStatus()), "该合同状态无法生效");
        exist.setStatus(ContractStatus.EFFECTIVE.getKey());
        updateToDatabase(exist);
    }

    /**
     * 终止合同
     *
     * @param id 合同 ID
     */
    public void stop(long id) {
        ContractEntity exist = get(id);
        FORBIDDEN.when(ContractStatus.EFFECTIVE.notEqualsKey(exist.getStatus()), "该合同状态无法终止");
        exist.setStatus(ContractStatus.TERMINATED.getKey());
        updateToDatabase(exist);
    }
}
