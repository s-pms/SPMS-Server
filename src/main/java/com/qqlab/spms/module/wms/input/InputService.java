package com.qqlab.spms.module.wms.input;

import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.helper.BillHelper;
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
        return bill.setStatus(InputStatus.INPUTTING.getValue());
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
        if (bill.getType() == InputType.PURCHASE.getValue()) {
            PurchaseEntity purchaseEntity = bill.getPurchase();
            BillHelper.updatePurchaseBill(purchaseEntity.setStatus(PurchaseStatus.FINISHED.getValue()));
        }
    }

    @Override
    public InputDetailEntity addFinish(InputDetailEntity detail) {
        InputDetailEntity savedDetail = detailService.getById(detail.getId());
        if (Objects.isNull(detail.getStorage()) || Objects.isNull(detail.getStorage().getId())) {
            Result.FORBIDDEN.show("请传入入库存储资源");
            return null;
        }
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(savedDetail.getMaterial().getId(), detail.getStorage().getId());
        if (Objects.nonNull(inventory)) {
            inventory.setQuantity(inventory.getQuantity() + detail.getQuantity());
            inventoryService.update(inventory);
        } else {
            inventory = new InventoryEntity()
                    .setQuantity(detail.getQuantity())
                    .setMaterial(detail.getMaterial())
                    .setType(InventoryType.STORAGE.getValue());
            inventoryService.add(inventory);
        }
        return super.addFinish(detail);
    }
}