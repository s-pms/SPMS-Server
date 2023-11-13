package com.qqlab.spms.module.mes.order.detail;

import com.qqlab.spms.base.bill.detail.BaseBillDetailRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface OrderDetailRepository extends BaseBillDetailRepository<OrderDetailEntity> {
}
