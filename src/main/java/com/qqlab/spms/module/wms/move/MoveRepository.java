package com.qqlab.spms.module.wms.move;

import com.qqlab.spms.base.bill.BaseBillRepository;
import com.qqlab.spms.module.wms.move.detail.MoveDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface MoveRepository extends BaseBillRepository<MoveEntity, MoveDetailEntity> {
}
