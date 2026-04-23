package cn.hamm.spms.module.channel.purchase;

import cn.hamm.airpower.core.NumberUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailRepository;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailService;
import cn.hamm.spms.module.channel.purchase.enums.PurchaseStatus;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.enums.InputStatus;
import cn.hamm.spms.module.wms.input.enums.InputType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cn.hamm.spms.module.system.config.enums.ConfigFlag.PURCHASE_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class PurchaseService extends AbstractBaseBillService<
        PurchaseEntity, PurchaseRepository,
        PurchaseDetailEntity, PurchaseDetailService, PurchaseDetailRepository
        > {
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
    public IDictionary getBillDetailsFinishStatus() {
        return PurchaseStatus.INPUTTING;
    }

    @Override
    public IDictionary getFinishedStatus() {
        return PurchaseStatus.DONE;
    }

    @Override
    protected void afterAllBillDetailFinished(long billId) {
        PurchaseEntity purchaseBill = get(billId);
        List<PurchaseDetailEntity> details = detailService.getAllByBillId(billId);
        List<InputDetailEntity> inputDetails = new ArrayList<>();
        double totalRealPrice = 0D;
        for (PurchaseDetailEntity detail : details) {
            totalRealPrice += detail.getPrice() * detail.getFinishQuantity();
            inputDetails.add(new InputDetailEntity()
                    .setQuantity(detail.getFinishQuantity())
                    .setMaterial(detail.getMaterial())
            );
        }
        purchaseBill.setTotalRealPrice(totalRealPrice);
        updateToDatabase(purchaseBill);
        log.info("采购单已经全部采购完成，单据ID:{}", purchaseBill.getId());

        // 创建采购入库单
        InputEntity inputBill = new InputEntity()
                .setStatus(InputStatus.AUDITING.getKey())
                .setPurchase(purchaseBill)
                .setType(InputType.PURCHASE.getKey())
                .setDetails(inputDetails);
        WmsServices.getInputService().add(inputBill);
        log.info("创建采购入库单：{}", inputBill);
    }

    @Override
    protected void afterDetailSaved(long purchaseId) {
        List<PurchaseDetailEntity> details = detailService.getAllByBillId(purchaseId);
        double totalPrice = details.stream()
                .mapToDouble(detail -> NumberUtil.multiply(detail.getPrice(), detail.getQuantity()))
                .sum();
        updateToDatabase(getEntityInstance(purchaseId).setTotalPrice(totalPrice));
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return PURCHASE_BILL_AUTO_AUDIT;
    }
}
