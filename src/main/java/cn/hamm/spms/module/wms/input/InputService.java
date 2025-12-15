package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.dictionary.DictionaryUtil;
import cn.hamm.airpower.dictionary.IDictionary;
import cn.hamm.airpower.reflect.ReflectUtil;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;
import cn.hamm.spms.module.wms.input.enums.InputStatus;
import cn.hamm.spms.module.wms.input.enums.InputType;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.enums.InventoryType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.INPUT_BILL_AUTO_AUDIT;

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
    public void afterBillFinished(@NotNull InputEntity inputBill) {
        InputType inputType = DictionaryUtil.getDictionary(InputType.class, inputBill.getType());
        log.info("入库单入库完成 {}，单据ID:{} {} 入库类型 {}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                inputBill.getId(),
                inputBill.getBillCode(),
                inputType.getLabel()
        );
        switch (inputType) {
            case PURCHASE -> Services.getPurchaseService().setBillFinished(inputBill.getPurchase().getId());
            case PRODUCTION -> Services.getOrderService().setBillFinished(inputBill.getOrder().getId());
            default -> {
            }
        }
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull InputDetailEntity inputDetail) {
        // 入库仓库信息
        StorageEntity storage = inputDetail.getStorage();
        if (Objects.isNull(storage) || Objects.isNull(storage.getId())) {
            FORBIDDEN.show("请传入入库仓库");
            return;
        }
        InputDetailEntity existDetail = detailService.get(inputDetail.getId());
        InventoryService inventoryService = Services.getInventoryService();

        // 查询库存信息
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(existDetail.getMaterial().getId(), storage.getId());

        // 入库数量
        Double inputDetailQuantity = inputDetail.getQuantity();

        if (Objects.nonNull(inventory)) {
            inventory.setQuantity(NumberUtil.add(inventory.getQuantity(), inputDetailQuantity));
            inventoryService.update(inventory);
            log.info("入库单明细更新库存完毕，单据ID: {}", inputDetail.getId());
            return;
        }
        inventory = new InventoryEntity()
                .setQuantity(inputDetailQuantity)
                .setMaterial(existDetail.getMaterial())
                .setStorage(storage)
                .setType(InventoryType.STORAGE.getKey());
        inventoryService.add(inventory);
        log.info("入库单明细创建库存完毕，单据ID: {}", inputDetail.getId());
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return INPUT_BILL_AUTO_AUDIT;
    }
}
