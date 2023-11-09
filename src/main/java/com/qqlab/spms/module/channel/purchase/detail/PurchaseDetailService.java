package com.qqlab.spms.module.channel.purchase.detail;

import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class PurchaseDetailService extends BaseService<PurchaseDetailEntity, PurchaseDetailRepository> {
    public List<PurchaseDetailEntity> getAllByBillId(Long billId) {
        return repository.getAllByBillId(billId);
    }

    public void deleteAllByBillId(Long billId) {
        List<PurchaseDetailEntity> details = getAllByBillId(billId);
        for (PurchaseDetailEntity detail : details) {
            repository.deleteById(detail.getId());
        }
    }
}
