package cn.hamm.spms.module.wms.move;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.result.Result;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.InputStatus;
import cn.hamm.spms.module.wms.input.InputType;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.InventoryType;
import cn.hamm.spms.module.wms.move.detail.MoveDetailEntity;
import cn.hamm.spms.module.wms.move.detail.MoveDetailRepository;
import cn.hamm.spms.module.wms.move.detail.MoveDetailService;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.OutputStatus;
import cn.hamm.spms.module.wms.output.OutputType;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class MoveService extends AbstractBaseBillService<MoveEntity, MoveRepository, MoveDetailEntity, MoveDetailService, MoveDetailRepository> {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private MoveDetailService moveDetailService;

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
    protected void afterAddDetailFinish(long detailId, MoveDetailEntity sourceDetail) {
        sourceDetail = detailService.get(detailId);
        if (sourceDetail.getInventory().getQuantity() < sourceDetail.getQuantity()) {
            // 判断来源库存
            Result.FORBIDDEN.show("库存信息不足" + sourceDetail.getQuantity());
        }

        // 扣除来源库存
        InventoryEntity from = sourceDetail.getInventory();
        from.setQuantity(from.getQuantity() - sourceDetail.getQuantity());
        inventoryService.update(from);

        // 查询移库单
        MoveEntity bill = get(sourceDetail.getBillId());
        InventoryEntity to = inventoryService.getByMaterialIdAndStorageId(sourceDetail.getInventory().getMaterial().getId(), bill.getStorage().getId());
        if (Objects.nonNull(to)) {
            // 更新目标库存
            to.setQuantity(to.getQuantity() + sourceDetail.getQuantity());
            inventoryService.update(to);
        } else {
            // 创建目标库存
            to = new InventoryEntity()
                    .setQuantity(sourceDetail.getQuantity())
                    .setMaterial(sourceDetail.getInventory().getMaterial())
                    .setStorage(bill.getStorage())
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(to);
        }
    }

    @Override
    public void afterAllDetailsFinished(Long id) {
        MoveEntity moveBill = get(id);
        moveBill.setStatus(MoveStatus.DONE.getKey());
        update(moveBill);


        InputEntity inputBill = new InputEntity()
                .setType(InputType.MOVE.getKey())
                .setMove(moveBill)
                .setStatus(InputStatus.DONE.getKey());
        OutputEntity outputBill = new OutputEntity()
                .setType(OutputType.MOVE.getKey())
                .setMove(moveBill)
                .setStatus(OutputStatus.DONE.getKey());
        List<MoveDetailEntity> details = moveDetailService.getAllByBillId(moveBill.getId());
        List<OutputDetailEntity> outputDetails = new ArrayList<>();
        List<InputDetailEntity> inputDetails = new ArrayList<>();
        for (MoveDetailEntity detail : details) {
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
        }
        inputBill.setDetails(inputDetails);
        Services.getInputService().add(inputBill);
        outputBill.setDetails(outputDetails);
        Services.getOutputService().add(outputBill);
    }
}