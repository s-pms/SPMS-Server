package com.qqlab.spms.module.channel.sale;

import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailEntity;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailService;
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
public class SaleService extends BaseService<SaleEntity, SaleRepository> {
    @Autowired
    private SaleDetailService detailService;

    @Override
    public SaleEntity add(SaleEntity entity) {
        return saveDetails(entity.getDetails(), addToDatabase(entity));
    }

    @Override
    public SaleEntity update(SaleEntity entity) {
        return saveDetails(entity.getDetails(), updateToDatabase(entity));
    }

    @Transactional(rollbackOn = Exception.class)
    SaleEntity saveDetails(List<SaleDetailEntity> details, SaleEntity savedEntity) {
        detailService.deleteAllByBillId(savedEntity.getId());
        double totalPrice = 0D;
        for (SaleDetailEntity detail : details) {
            detailService.add(detail.setBillId(savedEntity.getId()));
            totalPrice += detail.getQuantity() * detail.getPrice();
        }
        savedEntity.setTotalPrice(totalPrice);
        return updateToDatabase(savedEntity);
    }
}
