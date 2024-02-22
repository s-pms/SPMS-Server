package com.qqlab.spms.module.wms.output;

import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.common.helper.CommonServiceHelper;
import com.qqlab.spms.module.channel.sale.SaleEntity;
import com.qqlab.spms.module.channel.sale.SaleStatus;
import com.qqlab.spms.module.wms.inventory.InventoryEntity;
import com.qqlab.spms.module.wms.inventory.InventoryService;
import com.qqlab.spms.module.wms.output.detail.OutputDetailEntity;
import com.qqlab.spms.module.wms.output.detail.OutputDetailRepository;
import com.qqlab.spms.module.wms.output.detail.OutputDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class OutputService extends AbstractBaseBillService<OutputEntity, OutputRepository, OutputDetailEntity, OutputDetailService, OutputDetailRepository> {
    @Autowired
    private InventoryService inventoryService;

    @Override
    public OutputEntity setAudited(OutputEntity bill) {
        return bill.setStatus(OutputStatus.OUTPUTTING.getKey());
    }

    @Override
    public OutputEntity setAuditing(OutputEntity bill) {
        return bill.setStatus(OutputStatus.AUDITING.getKey());
    }

    @Override
    public boolean isAudited(OutputEntity bill) {
        return bill.getStatus() != OutputStatus.AUDITING.getKey();
    }

    @Override
    public boolean canReject(OutputEntity bill) {
        return bill.getStatus() == OutputStatus.AUDITING.getKey();
    }

    @Override
    public OutputEntity setReject(OutputEntity bill) {
        return bill.setStatus(OutputStatus.REJECTED.getKey());
    }

    @Override
    public boolean canEdit(OutputEntity bill) {
        return bill.getStatus() == OutputStatus.REJECTED.getKey();
    }

    @Override
    public void afterAllDetailsFinished(Long id) {
        OutputEntity bill = getById(id);
        bill.setStatus(OutputStatus.DONE.getKey());
        updateToDatabase(bill);
        if (bill.getType() == OutputType.SALE.getKey()) {
            SaleEntity saleEntity = bill.getSale();
            CommonServiceHelper.getSaleService().update(saleEntity.setStatus(SaleStatus.DONE.getKey()));
        }
    }

    @Override
    public OutputDetailEntity addFinish(OutputDetailEntity detail) {
        InventoryEntity inventory = detail.getInventory();
        if (Objects.isNull(inventory)) {
            Result.FORBIDDEN.show("请传入库存信息");
            return null;
        }
        inventory = inventoryService.get(inventory.getId());
        detail = detailService.get(detail.getId());
        Result.FORBIDDEN.whenNotEquals(inventory.getMaterial().getId(), detail.getMaterial().getId(), "物料信息不匹配");
        if (inventory.getQuantity() < detail.getQuantity()) {
            // 判断库存
            Result.FORBIDDEN.show("库存信息不足" + detail.getQuantity());
        }
        inventory.setQuantity(inventory.getQuantity() - detail.getQuantity());
        inventoryService.update(inventory);
        return super.addFinish(detail);
    }
}