package cn.hamm.spms.module.mes.order;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface OrderRepository extends BaseBillRepository<OrderEntity, OrderDetailEntity> {
}
