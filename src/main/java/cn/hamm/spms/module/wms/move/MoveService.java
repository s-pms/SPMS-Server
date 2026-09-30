package cn.hamm.spms.module.wms.move;

import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.MOVE_BILL_AUTO_AUDIT;

/**
 * <h1>移库单</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
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

    /**
     * 报工后在来源仓库扣减库存、把数量加到目标仓库
     *
     * @param detailId   移库明细 ID
     * @param moveDetail 移库明细
     */
    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull MoveDetailEntity moveDetail) {
        moveDetail = detailService.get(detailId);

        MoveEntity bill = get(moveDetail.getBillId());
        StorageEntity storage = bill.getStorage();
        Double moveDetailQuantity = moveDetail.getQuantity();

        InventoryService inventoryService = WmsServices.getInventoryService();

        InventoryEntity from = moveDetail.getInventory();
        FORBIDDEN.whenNull(from, "明细没有关联库存行，请先完善明细的库存信息");

        MaterialEntity material = from.getMaterial();

        transactionHelper.run(() -> {
            inventoryService.reduceInventoryQuantity(from.getId(), moveDetailQuantity);
            InventoryEntity to = inventoryService.getByMaterialIdAndStorageId(material.getId(), storage.getId());
            if (Objects.nonNull(to)) {
                inventoryService.addInventoryQuantity(to.getId(), moveDetailQuantity);
                return;
            }
            to = new InventoryEntity()
                    .setQuantity(moveDetailQuantity)
                    .setMaterial(material)
                    .setStorage(storage)
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(to);
        });
    }

    /**
     * 移库完成后生成入/出库记账凭证
     *
     * @param billId 移库单 ID
     * @apiNote 必须用 {@code addToDatabase} 而非 {@code add}：库存增减已在
     * {@code afterDetailFinishAdded} 里做完，这两张单状态直接是「已完成」，
     * 而 {@code add} 会触发自动审核，{@code canAudit} 判断「已完成」不是「审核中」直接抛异常，
     * 异常冒泡会回滚整个移库事务 —— 只要打开入/出库单自动审核，移库就整体不可用
     */
    @Override
    protected void afterAllBillDetailFinished(long billId) {
        MoveEntity moveBill = get(billId);
        List<MoveDetailEntity> details = detailService.getAllByBillId(billId);
        List<OutputDetailEntity> outputDetails = new ArrayList<>();
        List<InputDetailEntity> inputDetails = new ArrayList<>();
        details.forEach(detail -> {
            InventoryEntity inventory = detail.getInventory();
            FORBIDDEN.whenNull(inventory, "移库明细没有关联库存行，请先完善明细的库存信息");
            inputDetails.add(new InputDetailEntity()
                    .setStorage(moveBill.getStorage())
                    .setMaterial(inventory.getMaterial())
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(detail.getFinishQuantity())
            );
            outputDetails.add(new OutputDetailEntity()
                    .setInventory(inventory)
                    .setMaterial(inventory.getMaterial())
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(detail.getFinishQuantity())
            );
        });
        InputEntity inputBill = new InputEntity()
                .setType(InputType.MOVE.getKey())
                .setMove(moveBill)
                .setStatus(InputStatus.DONE.getKey());
        long inputId = WmsServices.getInputService().addToDatabase(inputBill);
        WmsServices.getInputDetailService().saveDetails(inputId, inputDetails);
        log.info("移库单 {} 已生成入库单 {}", billId, inputId);

        OutputEntity outputBill = new OutputEntity()
                .setType(OutputType.MOVE.getKey())
                .setMove(moveBill)
                .setStatus(OutputStatus.DONE.getKey());
        long outputId = WmsServices.getOutputService().addToDatabase(outputBill);
        WmsServices.getOutputDetailService().saveDetails(outputId, outputDetails);
        log.info("移库单 {} 已生成出库单 {}", billId, outputId);
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return MOVE_BILL_AUTO_AUDIT;
    }
}
