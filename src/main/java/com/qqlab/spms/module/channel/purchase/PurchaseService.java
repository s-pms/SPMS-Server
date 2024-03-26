package com.qqlab.spms.module.channel.purchase;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.common.helper.CommonServiceHelper;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailRepository;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailService;
import com.qqlab.spms.module.wms.input.InputEntity;
import com.qqlab.spms.module.wms.input.InputStatus;
import com.qqlab.spms.module.wms.input.detail.InputDetailEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        return bill.setStatus(PurchaseStatus.PURCHASING.getKey());
    }

    @Override
    public boolean isAudited(PurchaseEntity bill) {
        return bill.getStatus() != PurchaseStatus.AUDITING.getKey();
    }

    @Override
    public boolean canReject(PurchaseEntity bill) {
        return bill.getStatus() == PurchaseStatus.AUDITING.getKey();
    }

    @Override
    public PurchaseEntity setReject(PurchaseEntity bill) {
        return bill.setStatus(PurchaseStatus.REJECTED.getKey());
    }

    @Override
    public boolean canEdit(PurchaseEntity bill) {
        return bill.getStatus() == PurchaseStatus.REJECTED.getKey();
    }

    @Override
    public PurchaseEntity setAuditing(PurchaseEntity bill) {
        return bill.setStatus(PurchaseStatus.AUDITING.getKey());
    }

    @Override
    public void afterAllDetailsFinished(Long id) {
        PurchaseEntity bill = get(id);
        bill.setStatus(PurchaseStatus.DONE.getKey());
        List<PurchaseDetailEntity> details = detailService.getAllByBillId(bill.getId());
        List<InputDetailEntity> inputDetails = new ArrayList<>();
        double totalRealPrice = 0D;
        for (PurchaseDetailEntity detail : details) {
            totalRealPrice += detail.getPrice() * detail.getFinishQuantity();
            inputDetails.add(new InputDetailEntity()
                    .setQuantity(detail.getFinishQuantity())
                    .setMaterial(detail.getMaterial())
            );
        }
        bill.setTotalRealPrice(totalRealPrice);
        updateToDatabase(bill);

        // 创建采购入库单
        InputEntity inputBill = new InputEntity()
                .setStatus(InputStatus.AUDITING.getKey())
                .setPurchase(bill)
                .setDetails(inputDetails);
        CommonServiceHelper.getInputService().add(inputBill);
    }

    @Override
    protected void afterDetailSaved(PurchaseEntity purchase) {
        List<PurchaseDetailEntity> details = detailService.getAllByBillId(purchase.getId());
        double totalPrice = 0D;
        for (PurchaseDetailEntity detail : details) {
            totalPrice += detail.getPrice() * detail.getQuantity();
        }
        purchase.setTotalPrice(totalPrice);
        updateToDatabase(purchase);
    }
}
