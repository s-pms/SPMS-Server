package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.channel.sale.SaleStatus;
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
    public void afterAllDetailsFinished(Long id) {
        OutputEntity bill = get(id);
        bill.setStatus(OutputStatus.DONE.getKey());
        update(bill);
        if (OutputType.SALE.equalsKey(bill.getType())) {
            SaleEntity saleEntity = bill.getSale();
            Services.getSaleService().update(saleEntity.setStatus(SaleStatus.DONE.getKey()));
        }
    }

    @Override
    protected void afterAddDetailFinish(long detailId, @NotNull OutputDetailEntity sourceDetail) {
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
}