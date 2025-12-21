package cn.hamm.spms.module.channel.purchase;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.dictionary.IDictionary;
import cn.hamm.airpower.mcp.method.McpMethod;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailRepository;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailService;
import cn.hamm.spms.module.channel.purchase.enums.PurchaseStatus;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.enums.InputStatus;
import cn.hamm.spms.module.wms.input.enums.InputType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final MaterialService materialService;

    public PurchaseService(MaterialService materialService) {
        super();
        this.materialService = materialService;
    }

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
        Services.getInputService().add(inputBill);
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

    @McpMethod
    @Description("create purchase bill, includes reason/name/count")
    public String createPurchaseBill(
            @Description("purchase reason, required.")
            String reason,
            @Description("purchase name, required.")
            String name,
            @Description("purchase count, required.")
            Integer count) {
        List<MaterialEntity> materials = materialService.filter(new MaterialEntity().setName(name));
        if (materials.isEmpty()) {
            return "未找到该物料";
        } else if (materials.size() > 1) {
            return "找到多个该物料，" + materials.stream()
                    .map(MaterialEntity::getName)
                    .collect(Collectors.joining("/")) + "选哪一个采购？";
        }
        MaterialEntity material = materials.get(0);
        List<PurchaseDetailEntity> details = new ArrayList<>();
        details.add(new PurchaseDetailEntity()
                .setMaterial(material)
                .setQuantity(Double.valueOf(count))
                .setPrice(material.getPurchasePrice())
        );
        PurchaseEntity purchaseBill = new PurchaseEntity()
                .setReason(reason)
                .setDetails(details)
                .setStatus(PurchaseStatus.AUDITING.getKey());
        PurchaseEntity purchase = addAndGet(purchaseBill);
        return "采购单已经创建成功，单号为 " + purchase.getBillCode();
    }
}
