package com.qqlab.spms.module.mes.bom;

import com.qqlab.spms.base.bill.BaseBillRepository;
import com.qqlab.spms.module.mes.bom.detail.BomDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface BomRepository extends BaseBillRepository<BomEntity, BomDetailEntity> {
}
