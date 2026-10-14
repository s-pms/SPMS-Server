package cn.hamm.spms.module.channel.purchase;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>采购单</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PurchaseRepository extends BaseBillRepository<PurchaseEntity, PurchaseDetailEntity> {
}
