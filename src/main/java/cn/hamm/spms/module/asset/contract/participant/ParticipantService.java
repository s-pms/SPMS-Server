package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import org.springframework.stereotype.Service;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ParticipantService extends BaseService<ContractEntity, ParticipantRepository> {
    /**
     * 生效合同
     *
     * @param id 合同ID
     */
    public void enforce(long id) {
        ContractEntity exist = get(id);
        FORBIDDEN.when(ContractStatus.INVALID.notEqualsKey(exist.getStatus()), "该合同状态无法生效");
        exist.setStatus(ContractStatus.EFFECTIVE.getKey());
        update(exist);
    }

    /**
     * 终止合同
     *
     * @param id 合同ID
     */
    public void stop(long id) {
        ContractEntity exist = get(id);
        FORBIDDEN.when(ContractStatus.EFFECTIVE.notEqualsKey(exist.getStatus()), "该合同状态无法终止");
        exist.setStatus(ContractStatus.TERMINATED.getKey());
        update(exist);
    }
}
