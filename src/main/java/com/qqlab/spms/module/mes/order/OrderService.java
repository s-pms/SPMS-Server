package com.qqlab.spms.module.mes.order;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.module.mes.order.detail.OrderDetailEntity;
import com.qqlab.spms.module.mes.order.detail.OrderDetailRepository;
import com.qqlab.spms.module.mes.order.detail.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class OrderService extends AbstractBaseBillService<OrderEntity, OrderRepository, OrderDetailEntity, OrderDetailService, OrderDetailRepository> {
    @Override
    public OrderEntity setAudited(OrderEntity bill) {
        return bill.setStatus(OrderStatus.PRODUCING.getKey());
    }

    @Override
    public OrderEntity setAuditing(OrderEntity bill) {
        return bill.setStatus(OrderStatus.AUDITING.getKey());
    }

    @Override
    public boolean isAudited(OrderEntity bill) {
        return bill.getStatus() != OrderStatus.AUDITING.getKey();
    }

    @Override
    public boolean canReject(OrderEntity bill) {
        return bill.getStatus() == OrderStatus.AUDITING.getKey();
    }

    @Override
    public OrderEntity setReject(OrderEntity bill) {
        return bill.setStatus(OrderStatus.REJECTED.getKey());
    }

    @Override
    public boolean canEdit(OrderEntity bill) {
        return bill.getStatus() == OrderStatus.REJECTED.getKey();
    }
}