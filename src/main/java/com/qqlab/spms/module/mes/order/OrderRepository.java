package com.qqlab.spms.module.mes.order;

import com.qqlab.spms.base.bill.BaseBillRepository;
import com.qqlab.spms.module.mes.order.detail.OrderDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface OrderRepository extends BaseBillRepository<OrderEntity, OrderDetailEntity> {
}
