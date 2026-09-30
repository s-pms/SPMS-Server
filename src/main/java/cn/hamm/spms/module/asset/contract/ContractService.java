package cn.hamm.spms.module.asset.contract;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

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
