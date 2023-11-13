package com.qqlab.spms.module.channel.purchase;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailRepository;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class PurchaseService extends AbstractBaseBillService<PurchaseEntity, PurchaseRepository, PurchaseDetailEntity, PurchaseDetailService, PurchaseDetailRepository> {

    @Override
    public PurchaseEntity setAudited(PurchaseEntity bill) {
        return bill.setStatus(PurchaseStatus.PURCHASING.getValue());
    }

    @Override
    public boolean isAudited(PurchaseEntity bill) {
        return bill.getStatus() != PurchaseStatus.AUDITING.getValue();
    }

    @Override
    public boolean canReject(PurchaseEntity bill) {
        return bill.getStatus() == PurchaseStatus.AUDITING.getValue();
    }

    @Override
    public PurchaseEntity setReject(PurchaseEntity bill) {
        return bill.setStatus(PurchaseStatus.REJECTED.getValue());
    }

    @Override
    public boolean canEdit(PurchaseEntity bill) {
        return bill.getStatus() == PurchaseStatus.REJECTED.getValue();
    }

    @Override
    public PurchaseEntity setAuditing(PurchaseEntity bill) {
        return bill.setStatus(PurchaseStatus.AUDITING.getValue());
    }

    @Override
    protected PurchaseEntity afterDetailSaved(PurchaseEntity bill) {
        List<PurchaseDetailEntity> details = bill.getDetails();
        double totalPrice = 0D;
        for (PurchaseDetailEntity detail : details) {
            totalPrice += detail.getQuantity() * detail.getPrice();
        }
        bill.setTotalPrice(totalPrice);
        return updateToDatabase(bill);
    }
}
