package com.qqlab.spms.module.wms.move;

import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.module.wms.inventory.InventoryEntity;
import com.qqlab.spms.module.wms.inventory.InventoryService;
import com.qqlab.spms.module.wms.inventory.InventoryType;
import com.qqlab.spms.module.wms.move.detail.MoveDetailEntity;
import com.qqlab.spms.module.wms.move.detail.MoveDetailRepository;
import com.qqlab.spms.module.wms.move.detail.MoveDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class MoveService extends AbstractBaseBillService<MoveEntity, MoveRepository, MoveDetailEntity, MoveDetailService, MoveDetailRepository> {
    @Autowired
    private InventoryService inventoryService;

    @Override
    public MoveEntity setAudited(MoveEntity bill) {
        return bill.setStatus(MoveStatus.MOVING.getValue());
    }

    @Override
    public MoveEntity setAuditing(MoveEntity bill) {
        return bill.setStatus(MoveStatus.AUDITING.getValue());
    }

    @Override
    public boolean isAudited(MoveEntity bill) {
        return bill.getStatus() != MoveStatus.AUDITING.getValue();
    }

    @Override
    public boolean canReject(MoveEntity bill) {
        return bill.getStatus() == MoveStatus.AUDITING.getValue();
    }

    @Override
    public MoveEntity setReject(MoveEntity bill) {
        return bill.setStatus(MoveStatus.REJECTED.getValue());
    }

    @Override
    public boolean canEdit(MoveEntity bill) {
        return bill.getStatus() == MoveStatus.REJECTED.getValue();
    }

    @Override
    public MoveDetailEntity addFinish(MoveDetailEntity detail) {
        detail = detailService.getById(detail.getId());
        if (detail.getInventory().getQuantity() < detail.getQuantity()) {
            // 判断来源库存
            Result.FORBIDDEN.show("库存信息不足" + detail.getQuantity());
        }

        // 扣除来源库存
        InventoryEntity from = detail.getInventory();
        from.setQuantity(from.getQuantity() - detail.getQuantity());
        inventoryService.update(from);

        // 查询移库单
        MoveEntity bill = getById(detail.getBillId());
        InventoryEntity to = inventoryService.getByMaterialIdAndStorageId(detail.getInventory().getMaterial().getId(), bill.getToStorage().getId());
        if (Objects.nonNull(to)) {
            // 更新目标库存
            to.setQuantity(to.getQuantity() + detail.getQuantity());
            inventoryService.update(to);
        } else {
            // 创建目标库存
            to = new InventoryEntity()
                    .setQuantity(detail.getQuantity())
                    .setMaterial(detail.getInventory().getMaterial())
                    .setStorage(bill.getToStorage())
                    .setType(InventoryType.STORAGE.getValue());
            inventoryService.add(to);
        }
        return super.addFinish(detail);
    }

    @Override
    public void afterAllDetailsFinished(Long id) {
        MoveEntity bill = getById(id);
        bill.setStatus(MoveStatus.DONE.getValue());
        updateToDatabase(bill);
    }
}