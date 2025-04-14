package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ParticipantService extends BaseService<ContractEntity, ParticipantRepository> {
    /**
     * <h3>生效合同</h3>
     *
     * @param id 合同ID
     */
    public void enforce(long id) {
        ContractEntity exist = get(id);
        ServiceError.FORBIDDEN.when(ContractStatus.INVALID.notEqualsKey(exist.getStatus()), "该合同状态无法生效");
        exist.setStatus(ContractStatus.EFFECTIVE.getKey());
        update(exist);
    }

    /**
     * <h3>终止合同</h3>
     *
     * @param id 合同ID
     */
    public void stop(long id) {
        ContractEntity exist = get(id);
        ServiceError.FORBIDDEN.when(ContractStatus.EFFECTIVE.notEqualsKey(exist.getStatus()), "该合同状态无法终止");
        exist.setStatus(ContractStatus.TERMINATED.getKey());
        update(exist);
    }
}
