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

    /**
     * 新建合同
     *
     * @param source 客户端提交的合同
     * @return 处理后的合同
     * @apiNote 状态一律置为「未生效」，只能通过 {@link #enforce(long)} / {@link #stop(long)} 变更。
     * 若允许客户端在建单时直接指定，就等于凭空造出一张已生效甚至已终止的合同
     */
    @Override
    protected @NotNull ContractEntity beforeAdd(@NotNull ContractEntity source) {
        source.setStatus(ContractStatus.INVALID.getKey());
        return source;
    }

    /**
     * 修改合同
     *
     * @param source 客户端提交的合同
     * @return 处理后的合同
     * @apiNote 状态回填为库中原值。状态机「未生效 → 生效中 → 已终止」是单向的，
     * 放开直接改等于让已终止的合同一步复活成「生效中」。{@code enforce} / {@code stop}
     * 走 {@code updateToDatabase} 不经过本钩子，受控的状态流转不受影响
     */
    @Override
    protected @NotNull ContractEntity beforeUpdate(@NotNull ContractEntity source) {
        ContractEntity exist = get(source.getId());
        source.setStatus(exist.getStatus());
        return source;
    }

    /**
     * 读取合同时组装参与方与附件
     *
     * @param contract 合同
     * @return 组装后的合同
     * @apiNote 两个集合是 {@code @Transient}（关联由中间表实体承载），不会随实体自动加载，
     * 不显式回填前端就拿不到数据
     */
    @Override
    protected @NotNull ContractEntity afterAppGet(@NotNull ContractEntity contract) {
        fillLinks(contract);
        return contract;
    }

    /**
     * 批量读取合同时组装参与方与附件
     *
     * @param list 合同列表
     * @return 处理后的列表
     */
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

    /**
     * 新建后同步参与方与附件到中间表
     *
     * @param id     合同 ID
     * @param source 客户端提交的合同
     */
    @Override
    protected void afterAppAdd(long id, @NotNull ContractEntity source) {
        syncLinks(id, source);
    }

    /**
     * 修改后同步参与方与附件到中间表
     *
     * @param id     合同 ID
     * @param source 客户端提交的合同
     */
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
