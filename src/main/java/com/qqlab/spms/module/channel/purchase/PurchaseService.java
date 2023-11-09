package com.qqlab.spms.module.channel.purchase;

import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class PurchaseService extends BaseService<PurchaseEntity, PurchaseRepository> {
    @Autowired
    private PurchaseDetailService detailService;

    @Override
    public PurchaseEntity add(PurchaseEntity entity) {
        return saveDetails(entity.getDetails(), addToDatabase(entity));
    }

    @Override
    public PurchaseEntity update(PurchaseEntity entity) {
        return saveDetails(entity.getDetails(), updateToDatabase(entity));
    }

    @Transactional(rollbackOn = Exception.class)
    PurchaseEntity saveDetails(List<PurchaseDetailEntity> details, PurchaseEntity savedEntity) {
        detailService.deleteAllByBillId(savedEntity.getId());
        double totalPrice = 0D;
        for (PurchaseDetailEntity detail : details) {
            detailService.add(detail.setBillId(savedEntity.getId()));
            totalPrice += detail.getQuantity() * detail.getPrice();
        }
        savedEntity.setTotalPrice(totalPrice);
        return updateToDatabase(savedEntity);
    }
}
