package com.qqlab.spms.module.wms.input;

import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.common.helper.CommonServiceHelper;
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
        return bill.setStatus(InputStatus.INPUTTING.getKey());
    }

    @Override
    public InputEntity setAuditing(InputEntity bill) {
        return bill.setStatus(InputStatus.AUDITING.getKey());
    }

    @Override
    public boolean isAudited(InputEntity bill) {
        return bill.getStatus() != InputStatus.AUDITING.getKey();
    }

    @Override
    public boolean canReject(InputEntity bill) {
        return bill.getStatus() == InputStatus.AUDITING.getKey();
    }

    @Override
    public InputEntity setReject(InputEntity bill) {
        return bill.setStatus(InputStatus.REJECTED.getKey());
    }

    @Override
    public boolean canEdit(InputEntity bill) {
        return bill.getStatus() == InputStatus.REJECTED.getKey();
    }

    @Override
    public void afterAllDetailsFinished(Long id) {
        InputEntity bill = get(id);
        bill.setStatus(InputStatus.DONE.getKey());
        updateToDatabase(bill);
        if (bill.getType() == InputType.PURCHASE.getKey()) {
            PurchaseEntity purchaseEntity = bill.getPurchase();
            CommonServiceHelper.getPurchaseService().update(purchaseEntity.setStatus(PurchaseStatus.FINISHED.getKey()));
        }
    }

    @Override
    public InputDetailEntity addFinish(InputDetailEntity inputDetail) {
        InputDetailEntity savedDetail = detailService.get(inputDetail.getId());
        if (Objects.isNull(inputDetail.getStorage()) || Objects.isNull(inputDetail.getStorage().getId())) {
            Result.FORBIDDEN.show("请传入入库存储资源");
            return null;
        }
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(savedDetail.getMaterial().getId(), inputDetail.getStorage().getId());
        if (Objects.nonNull(inventory)) {
            inventory.setQuantity(inventory.getQuantity() + inputDetail.getQuantity());
            inventoryService.update(inventory);
        } else {
            inventory = new InventoryEntity()
                    .setQuantity(inputDetail.getQuantity())
                    .setMaterial(inputDetail.getMaterial())
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(inventory);
        }
        return super.addFinish(inputDetail);
    }
}