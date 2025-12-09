package cn.hamm.spms.module.wms.move;

import cn.hamm.airpower.dictionary.IDictionary;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.enums.InputStatus;
import cn.hamm.spms.module.wms.input.enums.InputType;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.enums.InventoryType;
import cn.hamm.spms.module.wms.move.detail.MoveDetailEntity;
import cn.hamm.spms.module.wms.move.detail.MoveDetailRepository;
import cn.hamm.spms.module.wms.move.detail.MoveDetailService;
import cn.hamm.spms.module.wms.move.enums.MoveStatus;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.enums.OutputStatus;
import cn.hamm.spms.module.wms.output.enums.OutputType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.MOVE_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class MoveService extends AbstractBaseBillService<MoveEntity, MoveRepository, MoveDetailEntity, MoveDetailService, MoveDetailRepository> {
    @Override
    public IDictionary getAuditedStatus() {
        return MoveStatus.MOVING;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return MoveStatus.AUDITING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return MoveStatus.REJECTED;
    }

    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return MoveStatus.DONE;
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, MoveDetailEntity moveDetail) {
        moveDetail = detailService.get(detailId);
        Double moveDetailQuantity = moveDetail.getQuantity();
        InventoryEntity from = moveDetail.getInventory();
        if (from.getQuantity() < moveDetailQuantity) {
            // 判断来源库存
            FORBIDDEN.show("库存信息不足" + moveDetailQuantity);
        }

        // 扣除来源库存
        from.setQuantity(NumberUtil.subtract(from.getQuantity(), moveDetailQuantity));
        InventoryService inventoryService = Services.getInventoryService();
        inventoryService.update(from);

        // 查询移库单
        MoveEntity bill = get(moveDetail.getBillId());
        InventoryEntity to = inventoryService.getByMaterialIdAndStorageId(from.getMaterial().getId(), bill.getStorage().getId());
        if (Objects.nonNull(to)) {
            // 更新目标库存
            to.setQuantity(NumberUtil.add(to.getQuantity(), moveDetailQuantity));
            inventoryService.update(to);
            return;
        }
        // 创建目标库存
        to = new InventoryEntity()
                .setQuantity(moveDetailQuantity)
                .setMaterial(from.getMaterial())
                .setStorage(bill.getStorage())
                .setType(InventoryType.STORAGE.getKey());
        inventoryService.add(to);
    }

    @Override
    protected void afterAllBillDetailFinished(long billId) {
        MoveEntity moveBill = get(billId);
        InputEntity inputBill = new InputEntity()
                .setType(InputType.MOVE.getKey())
                .setMove(moveBill)
                .setStatus(InputStatus.DONE.getKey());
        OutputEntity outputBill = new OutputEntity()
                .setType(OutputType.MOVE.getKey())
                .setMove(moveBill)
                .setStatus(OutputStatus.DONE.getKey());
        List<MoveDetailEntity> details = detailService.getAllByBillId(moveBill.getId());
        List<OutputDetailEntity> outputDetails = new ArrayList<>();
        List<InputDetailEntity> inputDetails = new ArrayList<>();
        details.forEach(detail -> {
            inputDetails.add(new InputDetailEntity()
                    .setStorage(moveBill.getStorage())
                    .setMaterial(detail.getInventory().getMaterial())
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(detail.getFinishQuantity())
            );
            outputDetails.add(new OutputDetailEntity()
                    .setInventory(detail.getInventory())
                    .setMaterial(detail.getInventory().getMaterial())
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(detail.getFinishQuantity())
            );
        });
        inputBill.setDetails(inputDetails);
        Services.getInputService().add(inputBill);
        outputBill.setDetails(outputDetails);
        Services.getOutputService().add(outputBill);
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return MOVE_BILL_AUTO_AUDIT;
    }
}
