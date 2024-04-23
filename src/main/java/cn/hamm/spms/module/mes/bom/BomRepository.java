package cn.hamm.spms.module.mes.bom;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface BomRepository extends BaseBillRepository<BomEntity, BomDetailEntity> {
}
