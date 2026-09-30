package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.ReflectUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.ChannelServices;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.mes.MesServices;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;
import cn.hamm.spms.module.wms.input.enums.InputStatus;
import cn.hamm.spms.module.wms.input.enums.InputType;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.enums.InventoryType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.INPUT_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
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
    public IDictionary getBillDetailsFinishStatus() {
        return InputStatus.DONE;
    }

    @Override
    protected void afterBillFinished(long billId) {
        InputEntity inputBill = get(billId);
        InputType inputType = DictionaryUtil.getDictionary(InputType.class, inputBill.getType());
        log.info("入库单入库完成 {}，单据ID:{} {} 入库类型 {}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId,
                inputBill.getBillCode(),
                inputType.getLabel()
        );
        switch (inputType) {
            case PURCHASE -> ChannelServices.getPurchaseService().setBillFinished(inputBill.getPurchase().getId());
            case PRODUCTION -> MesServices.getOrderService().setBillFinished(inputBill.getOrder().getId());
            default -> {
            }
        }
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull InputDetailEntity inputDetail) {
        // 一律以数据库中已保存的明细为准：若取请求参数中的 storage，
        // 客户端可以在报工时临时更换入库仓库，货就记到了别的仓库去
        InputDetailEntity existDetail = detailService.get(detailId);
        StorageEntity storage = existDetail.getStorage();
        FORBIDDEN.when(Objects.isNull(storage) || Objects.isNull(storage.getId()),
                "明细没有关联入库仓库，请先完善明细的仓库信息");
        MaterialEntity material = existDetail.getMaterial();
        FORBIDDEN.whenNull(material, "明细没有关联物料，请先完善明细的物料信息");
        InventoryService inventoryService = WmsServices.getInventoryService();

        // 查询库存信息
        InventoryEntity inventory = inventoryService.getByMaterialIdAndStorageId(material.getId(), storage.getId());

        // 本次入库数量
        Double inputDetailQuantity = inputDetail.getQuantity();

        if (Objects.nonNull(inventory)) {
            inventoryService.addInventoryQuantity(inventory.getId(), inputDetailQuantity);
            log.info("入库单明细更新库存完毕，单据ID: {}", inputDetail.getId());
            return;
        }
        inventory = new InventoryEntity()
                .setQuantity(inputDetailQuantity)
                .setMaterial(material)
                .setStorage(storage)
                .setType(InventoryType.STORAGE.getKey());
        inventoryService.add(inventory);
        log.info("入库单明细创建库存完毕，单据ID: {}", inputDetail.getId());
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return INPUT_BILL_AUTO_AUDIT;
    }
}
