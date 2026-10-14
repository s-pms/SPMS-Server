package cn.hamm.spms.module.mes.order;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>生产订单</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface OrderRepository extends BaseBillRepository<OrderEntity, OrderDetailEntity> {
}
