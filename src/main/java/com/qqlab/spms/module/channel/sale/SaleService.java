package com.qqlab.spms.module.channel.sale;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailEntity;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailRepository;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class SaleService extends AbstractBaseBillService<SaleEntity, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {
    @Override
    public SaleEntity setAudited(SaleEntity bill) {
        return bill.setStatus(SaleStatus.OUTPUTTING.getValue());
    }

    @Override
    public SaleEntity setAuditing(SaleEntity bill) {
        return bill.setStatus(SaleStatus.AUDITING.getValue());
    }

    @Override
    public boolean isAudited(SaleEntity bill) {
        return bill.getStatus() != SaleStatus.AUDITING.getValue();
    }

    @Override
    public boolean canReject(SaleEntity bill) {
        return bill.getStatus() == SaleStatus.AUDITING.getValue();
    }

    @Override
    public SaleEntity setReject(SaleEntity bill) {
        return bill.setStatus(SaleStatus.REJECTED.getValue());
    }

    @Override
    public boolean canEdit(SaleEntity bill) {
        return bill.getStatus() == SaleStatus.REJECTED.getValue();
    }

    @Override
    protected SaleEntity afterDetailSaved(SaleEntity bill) {
        List<SaleDetailEntity> details = bill.getDetails();
        double totalPrice = 0D;
        for (SaleDetailEntity detail : details) {
            totalPrice += detail.getQuantity() * detail.getPrice();
        }
        bill.setTotalPrice(totalPrice);
        bill = updateToDatabase(bill);
        return bill;
    }
}
