package com.qqlab.spms.module.channel.sale;

import com.qqlab.spms.base.bill.BaseBillRepository;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface SaleRepository extends BaseBillRepository<SaleEntity, SaleDetailEntity> {
}
