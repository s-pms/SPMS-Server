package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailRepository;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;
import cn.hamm.spms.module.wms.output.enums.OutputStatus;
import cn.hamm.spms.module.wms.output.enums.OutputType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.OUTPUT_BILL_AUTO_AUDIT;

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
    protected void afterBillFinished(long billId) {
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
        InventoryService inventoryService = Services.getInventoryService();

        // 出库明细
        OutputDetailEntity existDetail = detailService.get(outputDetail.getId());

        // 物料 ID
        Long materialId = existDetail.getMaterial().getId();

        // 出库单
        OutputEntity bill = get(existDetail.getBillId());

        // 库存信息
        InventoryEntity inventory = inventoryService.get(outputDetail.getInventory().getId());
        FORBIDDEN.whenNotEquals(inventory.getMaterial().getId(), materialId, "物料信息不匹配");
        transactionHelper.run(() -> {
            Double outputDetailQuantity = outputDetail.getQuantity();
            inventoryService.reduceInventoryQuantity(inventory.getId(), outputDetailQuantity);
            // 获取出库单类型
            OutputType outputType = DictionaryUtil.getDictionary(OutputType.class, bill.getType());
            switch (outputType) {
                case SALE -> Services.getSaleDetailService().updateDetailQuantity(
                        bill.getSale().getId(),
                        outputDetailQuantity,
                        Services.getSaleService(),
                        detail -> FORBIDDEN.whenNotEquals(
                                detail.getMaterial().getId(),
                                materialId,
                                "物料信息不匹配"));
                case PICKING -> Services.getPickingDetailService().updateDetailQuantity(
                        bill.getPicking().getId(),
                        outputDetailQuantity,
                        Services.getPickingService(), detail -> FORBIDDEN.whenNotEquals(
                                detail.getMaterial().getId(),
                                materialId,
                                "物料信息不匹配"));
                default -> {
                }
            }
        });
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return OUTPUT_BILL_AUTO_AUDIT;
    }
}
