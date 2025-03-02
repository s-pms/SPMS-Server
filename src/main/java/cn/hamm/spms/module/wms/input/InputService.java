package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.InventoryType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;
import static cn.hamm.spms.module.system.config.ConfigFlag.INPUT_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class InputService extends AbstractBaseBillService<InputEntity, InputRepository, InputDetailEntity, InputDetailService, InputDetailRepository> {
    @Override
    public IDictionary getAuditingStatus() {
        return InputStatus.AUDITING;
    }

    @Override
    public IDictionary getAuditedStatus() {
        return InputStatus.INPUTTING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return InputStatus.REJECTED;
    }

    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return InputStatus.DONE;
    }

    @Override
    public void afterBillFinished(Long billId) {
        InputEntity inputBill = get(billId);
        InputType inputType = DictionaryUtil.getDictionary(InputType.class, inputBill.getType());
        log.info("入库单入库完成，单据ID:{}, 入库类型:{}", billId, inputType.getLabel());
        switch (inputType) {
            case PURCHASE -> Services.getPurchaseService().setBillFinished(inputBill.getPurchase().getId());
            case PRODUCTION -> Services.getOrderService().setBillFinished(inputBill.getOrder().getId());
            default -> {
            }
        }
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull InputDetailEntity sourceDetail) {
        if (Objects.isNull(sourceDetail.getStorage()) || Objects.isNull(sourceDetail.getStorage().getId())) {
            FORBIDDEN.show("请传入入库仓库");
            return;
        }
        InputDetailEntity existDetail = detailService.get(sourceDetail.getId());
        InventoryService inventoryService = Services.getInventoryService();
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(existDetail.getMaterial().getId(), sourceDetail.getStorage().getId());
        if (Objects.nonNull(inventory)) {
            inventory.setQuantity(NumberUtil.add(inventory.getQuantity(), sourceDetail.getQuantity()));
            inventoryService.update(inventory);
        } else {
            inventory = new InventoryEntity()
                    .setQuantity(sourceDetail.getQuantity())
                    .setMaterial(existDetail.getMaterial())
                    .setStorage(sourceDetail.getStorage())
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(inventory);
        }
        log.info("入库单明细更新库存完毕，单据ID: {}", sourceDetail.getId());
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return INPUT_BILL_AUTO_AUDIT;
    }
}
