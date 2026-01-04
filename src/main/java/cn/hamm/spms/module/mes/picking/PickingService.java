package cn.hamm.spms.module.mes.picking;

import cn.hamm.airpower.web.dictionary.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailEntity;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailRepository;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailService;
import cn.hamm.spms.module.mes.picking.enums.PickingStatus;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
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

import static cn.hamm.spms.module.system.config.enums.ConfigFlag.PICKING_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
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

    @Override
    protected void afterBillAudited(long billId) {
        // 创建领料出库单
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
        Services.getOutputService().add(outputBill);
    }

    @Override
    protected void afterAllBillDetailFinished(long billId) {
        log.info("领料单所有明细都已完成，单据ID:{}", billId);
        PickingEntity pickingBill = get(billId);
        // 添加线边库存
        List<PickingDetailEntity> details = detailService.getAllByBillId(pickingBill.getId());
        InventoryService inventoryService = Services.getInventoryService();
        details.forEach(detail -> {
            // 查询库存信息
            InventoryEntity inventory = inventoryService.getByMaterialIdAndStructureId(detail.getMaterial().getId(), pickingBill.getStructure().getId());
            if (Objects.nonNull(inventory)) {
                inventory.setQuantity(NumberUtil.add(inventory.getQuantity(), detail.getFinishQuantity()));
                inventoryService.updateToDatabase(inventory);
                return;
            }
            inventory = new InventoryEntity()
                    .setQuantity(detail.getFinishQuantity())
                    .setMaterial(detail.getMaterial())
                    .setStructure(pickingBill.getStructure())
                    .setType(InventoryType.STRUCTURE.getKey());
            inventoryService.add(inventory);
        });
    }
}
