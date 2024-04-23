package cn.hamm.spms.module.channel.purchase;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailRepository;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailService;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.InputStatus;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class PurchaseService extends AbstractBaseBillService<PurchaseEntity, PurchaseRepository, PurchaseDetailEntity, PurchaseDetailService, PurchaseDetailRepository> {
    @Override
    public IDictionary getAuditingStatus() {
        return PurchaseStatus.AUDITING;
    }

    @Override
    public IDictionary getAuditedStatus() {
        return PurchaseStatus.PURCHASING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return PurchaseStatus.REJECTED;
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
        update(bill);

        // 创建采购入库单
        InputEntity inputBill = new InputEntity()
                .setStatus(InputStatus.AUDITING.getKey())
                .setPurchase(bill)
                .setDetails(inputDetails);
        Services.getInputService().add(inputBill);
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
