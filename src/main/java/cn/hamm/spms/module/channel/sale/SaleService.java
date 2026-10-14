package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.core.NumberUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import cn.hamm.spms.module.channel.sale.enums.SaleStatus;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.enums.OutputStatus;
import cn.hamm.spms.module.wms.output.enums.OutputType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.SALE_BILL_AUTO_AUDIT;

/**
 * <h1>销售单</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class SaleService extends AbstractBaseBillService<SaleEntity, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {
    @Override
    public IDictionary getRejectedStatus() {
        return SaleStatus.REJECTED;
    }

    @Override
    public IDictionary getAuditedStatus() {
        return SaleStatus.OUTPUTTING;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return SaleStatus.AUDITING;
    }

    /**
     * 明细全部完成后的状态
     *
     * @return 明细完成状态
     * @apiNote 必须返回终态，否则基类的 {@code status.equals(getFinishedStatus())} 恒不成立，
     * 销售单永远推不到「已完成」，自动生成的出库单也停在「出库中」，库存不减少
     */
    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return SaleStatus.DONE;
    }

    /**
     * 明细全部出库后生成销售出库单
     *
     * @param billId 销售单 ID
     * @apiNote 生成下游单据前基类已用状态条件更新做推进守卫，并发报工下本方法只会执行一次，
     * 出库单恰好 1 张；发货仓库与物料库存记录缺一不可，否则会抛 FORBIDDEN 中断整条流程
     */
    @Override
    protected void afterAllBillDetailFinished(long billId) {
        SaleEntity sale = get(billId);
        FORBIDDEN.whenNull(sale.getStorage(), "销售单未指定发货仓库，无法生成出库单");
        InventoryService inventoryService = WmsServices.getInventoryService();
        List<OutputDetailEntity> outputDetails = new ArrayList<>();
        for (SaleDetailEntity detail : detailService.getAllByBillId(billId)) {
            InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(
                    detail.getMaterial().getId(), sale.getStorage().getId());
            FORBIDDEN.whenNull(inventory, String.format("物料 %s 在仓库「%s」中没有库存记录，无法生成出库单",
                    detail.getMaterial().getName(), sale.getStorage().getName()));
            outputDetails.add(new OutputDetailEntity()
                    .setInventory(inventory)
                    .setMaterial(detail.getMaterial())
                    .setQuantity(detail.getFinishQuantity()));
        }
        OutputEntity outputBill = new OutputEntity()
                .setStatus(OutputStatus.AUDITING.getKey())
                .setType(OutputType.SALE.getKey())
                .setSale(sale)
                .setDetails(outputDetails);
        long outputId = WmsServices.getOutputService().add(outputBill);
        log.info("销售单明细全部完成，已生成销售出库单，saleId:{}, outputId:{}", billId, outputId);
    }

    @Override
    protected void afterDetailSaved(long billId) {
        List<SaleDetailEntity> details = detailService.getAllByBillId(billId);
        double totalPrice = details.stream()
                .mapToDouble(detail -> NumberUtil.multiply(
                        detail.getQuantity(), detail.getPrice())
                )
                .sum();
        updateToDatabase(getEntityInstance(billId).setTotalPrice(totalPrice));
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return SALE_BILL_AUTO_AUDIT;
    }
}
