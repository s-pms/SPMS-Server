package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.enums.Result;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.channel.purchase.PurchaseEntity;
import cn.hamm.spms.module.channel.purchase.PurchaseStatus;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class InputService extends AbstractBaseBillService<InputEntity, InputRepository, InputDetailEntity, InputDetailService, InputDetailRepository> {
    @Autowired
    private InventoryService inventoryService;

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
    public void afterAllDetailsFinished(Long id) {
        InputEntity bill = get(id);
        bill.setStatus(InputStatus.DONE.getKey());
        update(bill);
        if (InputType.PURCHASE.equalsKey(bill.getType())) {
            PurchaseEntity purchaseEntity = bill.getPurchase();
            Services.getPurchaseService().update(purchaseEntity.setStatus(PurchaseStatus.FINISHED.getKey()));
        }
    }

    @Override
    protected void afterAddDetailFinish(long detailId, @NotNull InputDetailEntity sourceDetail) {
        if (Objects.isNull(sourceDetail.getStorage()) || Objects.isNull(sourceDetail.getStorage().getId())) {
            Result.FORBIDDEN.show("请传入入库存储资源");
            return;
        }
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(sourceDetail.getMaterial().getId(), sourceDetail.getStorage().getId());
        if (Objects.nonNull(inventory)) {
            inventory.setQuantity(inventory.getQuantity() + sourceDetail.getQuantity());
            inventoryService.update(inventory);
        } else {
            inventory = new InventoryEntity()
                    .setQuantity(sourceDetail.getQuantity())
                    .setMaterial(sourceDetail.getMaterial())
                    .setStorage(sourceDetail.getStorage())
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(inventory);
        }
    }
}