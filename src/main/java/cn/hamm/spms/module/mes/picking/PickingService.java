package cn.hamm.spms.module.mes.picking;

import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailEntity;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailRepository;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailService;
import cn.hamm.spms.module.mes.picking.enums.PickingStatus;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.enums.InventoryType;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.enums.OutputType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.PICKING_BILL_AUTO_AUDIT;

/**
 * <h1>领料单</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class PickingService extends AbstractBaseBillService<PickingEntity, PickingRepository, PickingDetailEntity, PickingDetailService, PickingDetailRepository> {

    @Override
    public IDictionary getAuditingStatus() {
        return PickingStatus.AUDITING;
    }

    @Override
    public IDictionary getAuditedStatus() {
        return PickingStatus.OUTPUTTING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return PickingStatus.REJECTED;
    }

    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return PickingStatus.DONE;
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return PICKING_BILL_AUTO_AUDIT;
    }

    /**
     * 审核通过后生成领料出库单
     *
     * @param billId 领料单 ID
     */
    @Override
    protected void afterBillAudited(long billId) {
        List<OutputDetailEntity> details = new ArrayList<>();
        detailService.getAllByBillId(billId)
                .forEach(detail -> details.add(
                        new OutputDetailEntity()
                                .setQuantity(detail.getQuantity())
                                .setMaterial(detail.getMaterial())
                ));
        OutputEntity outputBill = new OutputEntity()
                .setPicking(get(billId))
                .setType(OutputType.PICKING.getKey())
                .setDetails(details);
        WmsServices.getOutputService().add(outputBill);
    }

    /**
     * 领料完成后把领到的料加到线边库存
     *
     * @param billId 领料单 ID
     * @apiNote 库存行是「先查后建」，并发领料时两个线程可能都查不到，
     * 直接插入会撞 {@code uk_inv_structure} 唯一索引并让整张领料单回滚，
     * 因此插入失败要回退为累加
     */
    @Override
    protected void afterAllBillDetailFinished(long billId) {
        log.info("领料单所有明细都已完成，单据ID:{}", billId);
        PickingEntity pickingBill = get(billId);
        FORBIDDEN.whenNull(pickingBill.getStructure(), "领料单未指定生产单元，无法回写线边库存");
        List<PickingDetailEntity> details = detailService.getAllByBillId(pickingBill.getId());
        InventoryService inventoryService = WmsServices.getInventoryService();
        transactionHelper.run(() -> {
            for (PickingDetailEntity detail : details) {
                if (Objects.isNull(detail.getMaterial())) {
                    FORBIDDEN.show("领料明细没有关联物料，请先完善明细信息");
                }
                double finishQuantity = Objects.requireNonNullElse(detail.getFinishQuantity(), 0D);
                if (finishQuantity <= 0) {
                    continue;
                }
                InventoryEntity inventory = inventoryService.getByMaterialIdAndStructureId(
                        detail.getMaterial().getId(), pickingBill.getStructure().getId());
                if (Objects.nonNull(inventory)) {
                    // 走 addInventoryQuantity 内部带行锁的累加，避免读-改-写丢更新
                    inventoryService.addInventoryQuantity(inventory.getId(), finishQuantity);
                    continue;
                }
                try {
                    inventoryService.add(new InventoryEntity()
                            .setQuantity(finishQuantity)
                            .setMaterial(detail.getMaterial())
                            .setStructure(pickingBill.getStructure())
                            .setType(InventoryType.STRUCTURE.getKey()));
                } catch (Exception e) {
                    // 并发下另一个线程已插入，回退为累加
                    InventoryEntity created = inventoryService.getByMaterialIdAndStructureId(
                            detail.getMaterial().getId(), pickingBill.getStructure().getId());
                    FORBIDDEN.whenNull(created, "写线边库存失败：" + e.getMessage());
                    inventoryService.addInventoryQuantity(created.getId(), finishQuantity);
                }
            }
        });
    }
}
