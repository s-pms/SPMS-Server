package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.SaleStatus;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailRepository;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class OutputService extends AbstractBaseBillService<OutputEntity, OutputRepository, OutputDetailEntity, OutputDetailService, OutputDetailRepository> {
    @Override
    public IDictionary getAuditedStatus() {
        return OutputStatus.OUTPUTTING;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return OutputStatus.AUDITING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return OutputStatus.REJECTED;
    }

    @Override
    public IDictionary getFinishedStatus() {
        return OutputStatus.DONE;
    }

    @Override
    public void afterBillFinished(Long billId) {
        OutputEntity outputBill = get(billId);
        if (OutputType.SALE.equalsKey(outputBill.getType())) {
            SaleService saleService = Services.getSaleService();
            SaleEntity saleBill = saleService.get(outputBill.getSale().getId());
            saleService.update(saleBill.setStatus(SaleStatus.DONE.getKey()));
        }
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull OutputDetailEntity sourceDetail) {
        InventoryEntity inventory = sourceDetail.getInventory();
        if (Objects.isNull(inventory)) {
            ServiceError.FORBIDDEN.show("请传入库存信息");
            return;
        }
        OutputDetailEntity existDetail = detailService.get(sourceDetail.getId());
        InventoryService inventoryService = Services.getInventoryService();
        inventory = inventoryService.get(inventory.getId());
        ServiceError.FORBIDDEN.whenNotEquals(inventory.getMaterial().getId(), existDetail.getMaterial().getId(), "物料信息不匹配");
        if (inventory.getQuantity() < sourceDetail.getQuantity()) {
            // 判断库存
            ServiceError.FORBIDDEN.show("库存信息不足" + sourceDetail.getQuantity());
        }
        inventory.setQuantity(inventory.getQuantity() - sourceDetail.getQuantity());
        inventoryService.update(inventory);
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return ConfigFlag.OUTPUT_ORDER_AUTO_AUDIT;
    }
}
