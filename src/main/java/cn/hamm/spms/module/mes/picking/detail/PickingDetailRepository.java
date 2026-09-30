package cn.hamm.spms.module.mes.picking.detail;

import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>领料明细</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PickingDetailRepository extends BaseBillDetailRepository<PickingDetailEntity> {
}
