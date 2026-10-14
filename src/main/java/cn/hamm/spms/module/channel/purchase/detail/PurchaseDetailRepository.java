package cn.hamm.spms.module.channel.purchase.detail;

import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>采购单明细</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PurchaseDetailRepository extends BaseBillDetailRepository<PurchaseDetailEntity> {
}
