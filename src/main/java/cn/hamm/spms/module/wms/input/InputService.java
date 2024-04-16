package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.result.Result;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
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
        if (bill.getType() == InputType.PURCHASE.getKey()) {
            PurchaseEntity purchaseEntity = bill.getPurchase();
            Services.getPurchaseService().update(purchaseEntity.setStatus(PurchaseStatus.FINISHED.getKey()));
        }
    }

    @Override
    protected InputDetailEntity beforeAddFinish(InputDetailEntity sourceDetail) {
        InputDetailEntity savedDetail = detailService.get(sourceDetail.getId());
        if (Objects.isNull(sourceDetail.getStorage()) || Objects.isNull(sourceDetail.getStorage().getId())) {
            Result.FORBIDDEN.show("请传入入库存储资源");
            return null;
        }
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(savedDetail.getMaterial().getId(), sourceDetail.getStorage().getId());
        if (Objects.nonNull(inventory)) {
            inventory.setQuantity(inventory.getQuantity() + savedDetail.getQuantity());
            inventoryService.update(inventory);
        } else {
            inventory = new InventoryEntity()
                    .setQuantity(sourceDetail.getQuantity())
                    .setMaterial(savedDetail.getMaterial())
                    .setStorage(sourceDetail.getStorage())
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(inventory);
        }
        return sourceDetail;
    }
}