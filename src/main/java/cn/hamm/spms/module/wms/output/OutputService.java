package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.dictionary.DictionaryUtil;
import cn.hamm.airpower.dictionary.IDictionary;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailRepository;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;
import static cn.hamm.spms.module.system.config.ConfigFlag.OUTPUT_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class OutputService extends AbstractBaseBillService<OutputEntity, OutputRepository, OutputDetailEntity, OutputDetailService, OutputDetailRepository> {
    @Override
    public IDictionary getAuditedStatus() {
        return OutputStatus.OUTPUTTING;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return OutputStatus.AUDITING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return OutputStatus.REJECTED;
    }

    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return OutputStatus.DONE;
    }

    @Override
    public void afterBillFinished(Long billId) {
        OutputEntity outputBill = get(billId);
        OutputType outputType = DictionaryUtil.getDictionary(OutputType.class, outputBill.getType());
        switch (outputType) {
            case SALE -> Services.getSaleService().setBillFinished(outputBill.getSale().getId());
            case PICKING -> Services.getPickingService().setBillFinished(outputBill.getPicking().getId());
            default -> {
            }
        }
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull OutputDetailEntity outputDetail) {
        InventoryEntity inventory = outputDetail.getInventory();
        FORBIDDEN.whenNull(inventory, "请传入库存信息");
        OutputDetailEntity existDetail = detailService.get(outputDetail.getId());
        InventoryService inventoryService = Services.getInventoryService();
        inventory = inventoryService.get(inventory.getId());
        FORBIDDEN.whenNotEquals(inventory.getMaterial().getId(), existDetail.getMaterial().getId(), "物料信息不匹配");
        FORBIDDEN.when(inventory.getQuantity() < outputDetail.getQuantity(), "库存信息不足" + outputDetail.getQuantity());
        inventory.setQuantity(NumberUtil.subtract(inventory.getQuantity(), outputDetail.getQuantity()));
        inventoryService.update(inventory);

        OutputEntity bill = get(existDetail.getBillId());
        OutputType outputType = DictionaryUtil.getDictionary(OutputType.class, bill.getType());
        switch (outputType) {
            case SALE -> Services.getSaleDetailService().updateDetailQuantity(
                    bill.getSale().getId(),
                    outputDetail.getQuantity(),
                    Services.getSaleService(),
                    detail -> FORBIDDEN.whenNotEquals(
                            detail.getMaterial().getId(),
                            existDetail.getMaterial().getId(),
                            "物料信息不匹配"));
            case PICKING -> Services.getPickingDetailService().updateDetailQuantity(
                    bill.getPicking().getId(),
                    outputDetail.getQuantity(),
                    Services.getPickingService(), detail -> FORBIDDEN.whenNotEquals(
                            detail.getMaterial().getId(),
                            existDetail.getMaterial().getId(),
                            "物料信息不匹配"));
            default -> {
            }
        }
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return OUTPUT_BILL_AUTO_AUDIT;
    }
}
