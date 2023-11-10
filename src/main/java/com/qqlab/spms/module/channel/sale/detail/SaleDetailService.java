package com.qqlab.spms.module.channel.sale.detail;

import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class SaleDetailService extends BaseService<SaleDetailEntity, SaleDetailRepository> {
    public List<SaleDetailEntity> getAllByBillId(Long billId) {
        return repository.getAllByBillId(billId);
    }

    public void deleteAllByBillId(Long billId) {
        List<SaleDetailEntity> details = getAllByBillId(billId);
        for (SaleDetailEntity detail : details) {
            repository.deleteById(detail.getId());
        }
    }
}
