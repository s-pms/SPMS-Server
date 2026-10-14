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
 * <h1>出库单</h1>
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

    /**
     * 出库完成后回写来源单据
     *
     * @param billId 出库单 ID
     */
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

    /**
     * 报工后扣减库存并回写来源单据明细数量
     *
     * @param detailId     出库明细 ID
     * @param outputDetail 出库明细
     * @apiNote 库存行与物料都必须取数据库中已保存的明细：取请求参数的话，
     * 客户端可在报工时临时更换目标库存行或物料，货就记到了别的账上。
     * 库存行是唯一真源，明细的 material 在级联保存时也可能尚未落库
     */
    @Override
    protected void afterDetailFinishAdded(long detailId, @NotNull OutputDetailEntity outputDetail) {
        InventoryService inventoryService = WmsServices.getInventoryService();

        OutputDetailEntity existDetail = detailService.get(detailId);
        InventoryEntity inventory = existDetail.getInventory();
        FORBIDDEN.whenNull(inventory, "明细没有关联库存行，请先完善明细的库存信息");

        MaterialEntity detailMaterial = inventory.getMaterial();
        FORBIDDEN.whenNull(detailMaterial, "库存行没有关联物料，请先完善库存信息");
        Long materialId = detailMaterial.getId();

        OutputEntity bill = get(existDetail.getBillId());
        transactionHelper.run(() -> {
            Double outputDetailQuantity = outputDetail.getQuantity();
            inventoryService.reduceInventoryQuantity(inventory.getId(), outputDetailQuantity);
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
