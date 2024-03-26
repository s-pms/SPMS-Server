package cn.hamm.spms.module.channel.purchase;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface PurchaseRepository extends BaseBillRepository<PurchaseEntity, PurchaseDetailEntity> {
}
