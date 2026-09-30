package cn.hamm.spms.module.wms.move;

import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import lombok.extern.slf4j.Slf4j;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.MOVE_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
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

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull MoveDetailEntity moveDetail) {
        moveDetail = detailService.get(detailId);

        // 查询移库单
        MoveEntity bill = get(moveDetail.getBillId());

        // 目标存储文字
        StorageEntity storage = bill.getStorage();

        // 本次移动数量
        Double moveDetailQuantity = moveDetail.getQuantity();

        InventoryService inventoryService = WmsServices.getInventoryService();

        // 来源库存信息
        InventoryEntity from = moveDetail.getInventory();
        FORBIDDEN.whenNull(from, "明细没有关联库存行，请先完善明细的库存信息");

        // 物料信息
        MaterialEntity material = from.getMaterial();

        transactionHelper.run(() -> {
            // 扣除来源库存
            inventoryService.reduceInventoryQuantity(from.getId(), moveDetailQuantity);

            // 查询目标库信息
            InventoryEntity to = inventoryService.getByMaterialIdAndStorageId(material.getId(), storage.getId());
            if (Objects.nonNull(to)) {
                // 更新目标库存
                inventoryService.addInventoryQuantity(to.getId(), moveDetailQuantity);
                return;
            }
            // 创建目标库存
            to = new InventoryEntity()
                    .setQuantity(moveDetailQuantity)
                    .setMaterial(material)
                    .setStorage(storage)
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(to);
        });
    }

    @Override
    protected void afterAllBillDetailFinished(long billId) {
        MoveEntity moveBill = get(billId);
        List<MoveDetailEntity> details = detailService.getAllByBillId(billId);
        List<OutputDetailEntity> outputDetails = new ArrayList<>();
        List<InputDetailEntity> inputDetails = new ArrayList<>();
        details.forEach(detail -> {
            // 库存信息
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
        // 添加入库单。
        // 这里必须用 addToDatabase 而非 add：移库单的库存增减已经在
        // afterDetailFinishAdded 里做完，生成的这张入库单/出库单只是记账凭证，
        // 状态直接是「已完成」。而 add 会触发 afterAppAdd 里的自动审核，
        // canAudit 判断「已完成」不是「审核中」直接抛异常，
        // 异常冒泡会把整个移库事务回滚 —— 只要打开入/出库单自动审核，
        // 移库功能就整体不可用。addToDatabase 不触发前后置钩子，正合这里的需求。
        InputEntity inputBill = new InputEntity()
                .setType(InputType.MOVE.getKey())
                .setMove(moveBill)
                .setStatus(InputStatus.DONE.getKey());
        long inputId = WmsServices.getInputService().addToDatabase(inputBill);
        WmsServices.getInputDetailService().saveDetails(inputId, inputDetails);
        log.info("移库单 {} 已生成入库单 {}", billId, inputId);

        // 添加出库单
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
