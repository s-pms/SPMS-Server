package cn.hamm.spms.module.mes.order.detail;

import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>生产订单明细</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface OrderDetailRepository extends BaseBillDetailRepository<OrderDetailEntity> {
}
