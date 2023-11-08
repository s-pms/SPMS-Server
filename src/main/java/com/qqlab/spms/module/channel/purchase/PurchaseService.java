package com.qqlab.spms.module.channel.purchase;

import com.qqlab.spms.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class PurchaseService extends BaseService<PurchaseEntity, PurchaseRepository> {
    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Override
    protected PurchaseEntity afterSaveToDatabase(PurchaseEntity entity) {
//        List<PurchaseDetailEntity> existList = purchaseDetailService.getAllByPurchaseId(entity.getId());
//        for (PurchaseDetailEntity exist : existList) {
//            purchaseDetailService.deleteById(exist.getId());
//        }
        List<PurchaseDetailEntity> detailEntitySet = entity.getPurchaseDetail();
        List<PurchaseDetailEntity> newList = new ArrayList<>();
        Double totalPrice = 0D;
        for (PurchaseDetailEntity detail : detailEntitySet) {
            detail.setPurchaseId(entity.getId());
            newList.add(purchaseDetailService.add(detail));
            totalPrice += detail.getQuantity() * detail.getPurchasePrice();
        }
        entity.setPurchaseDetail(newList);
        entity.setTotalPrice(totalPrice);
        return update(entity);
    }

    @Override
    protected PurchaseEntity afterGetById(PurchaseEntity entity) {
        return entity.setPurchaseDetail(purchaseDetailService.getAllByPurchaseId(entity.getId()));
    }
}
