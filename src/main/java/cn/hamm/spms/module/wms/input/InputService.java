package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.channel.purchase.PurchaseEntity;
import cn.hamm.spms.module.channel.purchase.PurchaseService;
import cn.hamm.spms.module.channel.purchase.PurchaseStatus;
import cn.hamm.spms.module.mes.order.OrderEntity;
import cn.hamm.spms.module.mes.order.OrderService;
import cn.hamm.spms.module.mes.order.OrderStatus;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class InputService extends AbstractBaseBillService<InputEntity, InputRepository, InputDetailEntity, InputDetailService, InputDetailRepository> {
    @Override
    public IDictionary getAuditingStatus() {
        return InputStatus.AUDITING;
    }

    @Override
    public IDictionary getAuditedStatus() {
        return InputStatus.INPUTTING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return InputStatus.REJECTED;
    }

    @Override
    public IDictionary getFinishedStatus() {
        return InputStatus.DONE;
    }

    @Override
    public void afterBillFinished(Long billId) {
        InputEntity inputBill = get(billId);
        InputType inputType = DictionaryUtil.getDictionary(InputType.class, inputBill.getType());
        switch (inputType) {
            case PURCHASE -> {
                PurchaseService purchaseService = Services.getPurchaseService();
                PurchaseEntity purchaseBill = purchaseService.get(inputBill.getPurchase().getId());
                purchaseService.update(purchaseBill.setStatus(PurchaseStatus.DONE.getKey()));
            }
            case PRODUCTION -> {
                OrderService orderService = Services.getOrderService();
                OrderEntity orderBill = orderService.get(inputBill.getOrder().getId());
                orderService.update(orderBill.setStatus(OrderStatus.DONE.getKey()));
            }
            default -> {
            }
        }
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull InputDetailEntity sourceDetail) {
        if (Objects.isNull(sourceDetail.getStorage()) || Objects.isNull(sourceDetail.getStorage().getId())) {
            ServiceError.FORBIDDEN.show("请传入入库存储资源");
            return;
        }
        InputDetailEntity existDetail = detailService.get(sourceDetail.getId());
        InventoryService inventoryService = Services.getInventoryService();
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(existDetail.getMaterial().getId(), sourceDetail.getStorage().getId());
        if (Objects.nonNull(inventory)) {
            inventory.setQuantity(inventory.getQuantity() + sourceDetail.getQuantity());
            inventoryService.update(inventory);
        } else {
            inventory = new InventoryEntity()
                    .setQuantity(sourceDetail.getQuantity())
                    .setMaterial(existDetail.getMaterial())
                    .setStorage(sourceDetail.getStorage())
                    .setType(InventoryType.STORAGE.getKey());
            inventoryService.add(inventory);
        }
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return ConfigFlag.INPUT_ORDER_AUTO_AUDIT;
    }
}
