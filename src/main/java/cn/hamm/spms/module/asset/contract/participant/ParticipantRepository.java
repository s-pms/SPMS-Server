package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface ParticipantRepository extends BaseRepository<ContractEntity> {
}
