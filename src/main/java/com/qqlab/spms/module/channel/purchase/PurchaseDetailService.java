package com.qqlab.spms.module.channel.purchase;

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
    List<PurchaseDetailEntity> getAllByPurchaseId(Long purchaseId){
        return repository.getAllByPurchaseId(purchaseId);
    }
}
