package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.ChannelServices;
import cn.hamm.spms.module.mes.MesServices;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailRepository;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;
import cn.hamm.spms.module.wms.output.enums.OutputStatus;
import cn.hamm.spms.module.wms.output.enums.OutputType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.spms.module.system.config.enums.ConfigFlag.OUTPUT_BILL_AUTO_AUDIT;

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
    public IDictionary getBillDetailsFinishStatus() {
        return OutputStatus.DONE;
    }

    @Override
    protected void afterBillFinished(long billId) {
        OutputEntity outputBill = get(billId);
        OutputType outputType = DictionaryUtil.getDictionary(OutputType.class, outputBill.getType());
        switch (outputType) {
            case SALE -> ChannelServices.getSaleService().setBillFinished(outputBill.getSale().getId());
            case PICKING -> MesServices.getPickingService().setBillFinished(outputBill.getPicking().getId());
            default -> {
            }
        }
    }

    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull OutputDetailEntity outputDetail) {
        InventoryService inventoryService = WmsServices.getInventoryService();

        // 出库明细，一律以数据库中已保存的明细为准
        OutputDetailEntity existDetail = detailService.get(detailId);

        // 库存信息，必须取明细入库时已保存的库存行；
        // 若取请求参数中的 inventory，客户端可在报工时临时更换目标库存行
        InventoryEntity inventory = existDetail.getInventory();
        FORBIDDEN.whenNull(inventory, "明细没有关联库存行，请先完善明细的库存信息");

        // 物料以库存行为准：库存行是唯一真源，客户端传入的 material 不可信，
        // 且明细的 material 字段在级联保存时可能未落库
        MaterialEntity detailMaterial = inventory.getMaterial();
        FORBIDDEN.whenNull(detailMaterial, "库存行没有关联物料，请先完善库存信息");
        Long materialId = detailMaterial.getId();

        // 出库单
        OutputEntity bill = get(existDetail.getBillId());
        transactionHelper.run(() -> {
            // 本次出库数量
            Double outputDetailQuantity = outputDetail.getQuantity();
            inventoryService.reduceInventoryQuantity(inventory.getId(), outputDetailQuantity);
            // 获取出库单类型
            OutputType outputType = DictionaryUtil.getDictionary(OutputType.class, bill.getType());
            switch (outputType) {
                case SALE -> ChannelServices.getSaleDetailService().updateDetailQuantity(
                        bill.getSale().getId(),
                        outputDetailQuantity,
                        ChannelServices.getSaleService(),
                        detail -> FORBIDDEN.whenNotEquals(
                                detail.getMaterial().getId(),
                                materialId,
                                "物料信息不匹配"));
                case PICKING -> MesServices.getPickingDetailService().updateDetailQuantity(
                        bill.getPicking().getId(),
                        outputDetailQuantity,
                        MesServices.getPickingService(), detail -> FORBIDDEN.whenNotEquals(
                                detail.getMaterial().getId(),
                                materialId,
                                "物料信息不匹配"));
                default -> {
                }
            }
        });
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return OUTPUT_BILL_AUTO_AUDIT;
    }
}
