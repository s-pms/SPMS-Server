package com.qqlab.spms.module.wms.input;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.helper.PurchaseInputHelper;
import com.qqlab.spms.module.channel.purchase.PurchaseEntity;
import com.qqlab.spms.module.channel.purchase.PurchaseStatus;
import com.qqlab.spms.module.wms.input.detail.InputDetailEntity;
import com.qqlab.spms.module.wms.input.detail.InputDetailRepository;
import com.qqlab.spms.module.wms.input.detail.InputDetailService;
import com.qqlab.spms.module.wms.inventory.InventoryEntity;
import com.qqlab.spms.module.wms.inventory.InventoryService;
import com.qqlab.spms.module.wms.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public InputEntity setAudited(InputEntity bill) {
        return bill.setStatus(InputStatus.INPUTING.getValue());
    }

    @Override
    public InputEntity setAuditing(InputEntity bill) {
        return bill.setStatus(InputStatus.AUDITING.getValue());
    }

    @Override
    public boolean isAudited(InputEntity bill) {
        return bill.getStatus() != InputStatus.AUDITING.getValue();
    }

    @Override
    public boolean canReject(InputEntity bill) {
        return bill.getStatus() == InputStatus.AUDITING.getValue();
    }

    @Override
    public InputEntity setReject(InputEntity bill) {
        return bill.setStatus(InputStatus.REJECTED.getValue());
    }

    @Override
    public boolean canEdit(InputEntity bill) {
        return bill.getStatus() == InputStatus.REJECTED.getValue();
    }

    @Override
    public void afterAllDetailsFinished(Long id) {
        InputEntity bill = getById(id);
        bill.setStatus(InputStatus.DONE.getValue());
        updateToDatabase(bill);
        // 查询所有明细 入库到指定的位置
        List<InputDetailEntity> details = detailService.getAllByBillId(id);
        for (InputDetailEntity detail : details) {
            InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(detail.getMaterial().getId(), bill.getStorage().getId());
            if (Objects.nonNull(inventory)) {
                inventory.setQuantity(inventory.getQuantity() + detail.getFinishQuantity());
                inventoryService.update(inventory);
            } else {
                inventory = new InventoryEntity()
                        .setQuantity(detail.getFinishQuantity())
                        .setMaterial(detail.getMaterial())
                        .setStorage(bill.getStorage())
                        .setType(InventoryType.STORAGE.getValue());
                inventoryService.add(inventory);
            }
        }
        if (bill.getType() == InputType.PURCHASE.getValue()) {
            PurchaseEntity purchaseEntity = bill.getPurchase();
            PurchaseInputHelper.updatePurchaseBill(purchaseEntity.setStatus(PurchaseStatus.FINISHED.getValue()));
        }
    }
}