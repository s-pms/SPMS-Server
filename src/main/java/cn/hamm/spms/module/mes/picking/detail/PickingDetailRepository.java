package cn.hamm.spms.module.mes.picking.detail;

import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PickingDetailRepository extends BaseBillDetailRepository<PickingDetailEntity> {
}
