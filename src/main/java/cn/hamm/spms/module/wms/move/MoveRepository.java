package cn.hamm.spms.module.wms.move;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.wms.move.detail.MoveDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>移库单</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface MoveRepository extends BaseBillRepository<MoveEntity, MoveDetailEntity> {
}
