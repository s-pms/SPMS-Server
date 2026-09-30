package cn.hamm.spms.module.asset.contract;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.AssetServices;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ContractService extends BaseService<ContractEntity, ContractRepository> {

    /**
     * 新建合同一律为「未生效」
     * <p>
     * 合同状态只能通过 {@link #enforce(long)} / {@link #stop(long)} 变更，
     * 新建时若允许客户端直接指定，就等于凭空造出一张已生效甚至已终止的合同。
     * </p>
     *
     * @param source 客户端提交的合同
     * @return 处理后的合同
     */
    @Override
    protected @NotNull ContractEntity beforeAdd(@NotNull ContractEntity source) {
        source.setStatus(ContractStatus.INVALID.getKey());
        return source;
    }

    /**
     * 修改时锁定合同状态
     * <p>
     * 状态机是「未生效 → 生效中 → 已终止」单向的，一旦允许直接改状态，
     * 已终止的合同能一步改回「生效中」（相当于让作废合同复活），
     * 未生效的合同也能跳过 {@link #enforce(long)} 的状态校验直接生效。
     * <p>
     * {@code enforce} / {@code stop} 走的是 {@code updateToDatabase}，
     * 不经过本钩子，因此受控的状态流转不受影响。
     * </p>
     *
     * @param source 客户端提交的合同
     * @return 处理后的合同
     */
    @Override
    protected @NotNull ContractEntity beforeUpdate(@NotNull ContractEntity source) {
        ContractEntity exist = get(source.getId());
        source.setStatus(exist.getStatus());
        return source;
    }

    /**
     * 读取合同时组装参与方与附件
     * <p>
     * 这两个集合已改为 {@code @Transient}（关联由中间表实体承载），
     * 因此不再随实体自动加载，必须显式组装后回填，否则前端拿不到数据。
     * </p>
     *
     * @param contract 合同
     * @return 组装后的合同
     */
    @Override
    protected @NotNull ContractEntity afterAppGet(@NotNull ContractEntity contract) {
        fillLinks(contract);
        return contract;
    }

    /**
     * 批量读取时同样需要组装
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
     */
    private void syncLinks(long contractId, @NotNull ContractEntity source) {
        AssetServices.getContractParticipantLinkService()
                .syncByContractId(contractId, source.getParticipantList());
        AssetServices.getContractDocumentLinkService()
                .syncByContractId(contractId, source.getDocumentList());
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
