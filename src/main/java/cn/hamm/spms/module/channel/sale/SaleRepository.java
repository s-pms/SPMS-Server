package cn.hamm.spms.module.channel.sale;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>销售单</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface SaleRepository extends BaseBillRepository<SaleEntity, SaleDetailEntity> {
}
