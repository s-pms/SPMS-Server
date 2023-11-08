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
public class PurchaseService extends BaseService<PurchaseEntity, PurchaseRepository> {
    @Override
    public PurchaseEntity add(PurchaseEntity entity) {
        List<PurchaseDetailEntity> detailList = entity.getPurchaseDetail();
        double totalPrice = 0D;
        for (PurchaseDetailEntity detail : detailList) {
            totalPrice += detail.getQuantity() * detail.getPurchasePrice();
        }
        entity.setTotalPrice(totalPrice);
        return addToDatabase(entity);
    }
}
