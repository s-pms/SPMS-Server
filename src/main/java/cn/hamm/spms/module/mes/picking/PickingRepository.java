package cn.hamm.spms.module.mes.picking;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>领料单</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PickingRepository extends BaseBillRepository<PickingEntity, PickingDetailEntity> {
}
