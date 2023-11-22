package com.qqlab.spms.helper;

import com.qqlab.spms.module.channel.purchase.PurchaseEntity;
import com.qqlab.spms.module.channel.purchase.PurchaseService;
import com.qqlab.spms.module.wms.input.InputEntity;
import com.qqlab.spms.module.wms.input.InputService;
import com.qqlab.spms.module.wms.input.InputType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>采购入库助手类</h1>
 *
 * @author hamm
 */
@Component
public class PurchaseInputHelper {
    private static PurchaseService purchaseService;
    private static InputService inputService;

    /**
     * <h2>添加采购入库单</h2>
     *
     * @param inputEntity 采购入库单
     * @return 采购入库单
     */
    public static InputEntity addInputBill(InputEntity inputEntity) {
        return inputService.add(inputEntity
                .setType(InputType.PURCHASE.getValue())
        );
    }

    /**
     * <h2>更新采购单</h2>
     *
     * @param purchaseEntity 采购单
     * @return 采购单
     */
    public static PurchaseEntity updatePurchaseBill(PurchaseEntity purchaseEntity) {
        return purchaseService.update(purchaseEntity);
    }

    @Autowired
    private void initService(
            PurchaseService purchaseService,
            InputService inputService
    ) {
        PurchaseInputHelper.purchaseService = purchaseService;
        PurchaseInputHelper.inputService = inputService;
    }
}
