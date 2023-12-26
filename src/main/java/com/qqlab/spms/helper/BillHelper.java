package com.qqlab.spms.helper;

import com.qqlab.spms.module.channel.purchase.PurchaseEntity;
import com.qqlab.spms.module.channel.purchase.PurchaseService;
import com.qqlab.spms.module.channel.sale.SaleEntity;
import com.qqlab.spms.module.channel.sale.SaleService;
import com.qqlab.spms.module.wms.input.InputEntity;
import com.qqlab.spms.module.wms.input.InputService;
import com.qqlab.spms.module.wms.output.OutputEntity;
import com.qqlab.spms.module.wms.output.OutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>采购入库助手类</h1>
 *
 * @author Hamm
 */
@Component
@SuppressWarnings("UnusedReturnValue")
public class BillHelper {
    private static PurchaseService purchaseService;
    private static SaleService saleService;
    private static InputService inputService;
    private static OutputService outputService;

    /**
     * 添加入库单
     *
     * @param inputEntity 入库单
     * @return 入库单
     */
    public static InputEntity addInputBill(InputEntity inputEntity) {
        return inputService.add(inputEntity);
    }

    /**
     * 添加出库单
     *
     * @param outputEntity 出库单
     * @return 出库单
     */
    public static OutputEntity addOutputBill(OutputEntity outputEntity) {
        return outputService.add(outputEntity);
    }

    /**
     * 更新采购单
     *
     * @param purchaseEntity 采购单
     * @return 采购单
     */
    public static PurchaseEntity updatePurchaseBill(PurchaseEntity purchaseEntity) {
        return purchaseService.update(purchaseEntity);
    }

    /**
     * 更新销售单
     *
     * @param saleEntity 销售单
     * @return 销售单
     */
    public static SaleEntity updateSaleBill(SaleEntity saleEntity) {
        return saleService.update(saleEntity);
    }

    @Autowired
    private void initService(
            PurchaseService purchaseService,
            SaleService saleService,
            InputService inputService,
            OutputService outputService
    ) {
        BillHelper.purchaseService = purchaseService;
        BillHelper.saleService = saleService;
        BillHelper.inputService = inputService;
        BillHelper.outputService = outputService;
    }
}
