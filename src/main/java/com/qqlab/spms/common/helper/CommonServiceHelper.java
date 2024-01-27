package com.qqlab.spms.common.helper;

import com.qqlab.spms.module.channel.purchase.PurchaseService;
import com.qqlab.spms.module.channel.sale.SaleService;
import com.qqlab.spms.module.wms.input.InputService;
import com.qqlab.spms.module.wms.output.OutputService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>采购入库助手类</h1>
 *
 * @author Hamm
 */
@Component
@SuppressWarnings("UnusedReturnValue")
public class CommonServiceHelper {
    @Getter
    private static PurchaseService purchaseService;

    @Getter
    private static SaleService saleService;

    @Getter
    private static InputService inputService;

    @Getter
    private static OutputService outputService;

    @Autowired
    private void initService(
            PurchaseService purchaseService,
            SaleService saleService,
            InputService inputService,
            OutputService outputService
    ) {
        CommonServiceHelper.purchaseService = purchaseService;
        CommonServiceHelper.saleService = saleService;
        CommonServiceHelper.inputService = inputService;
        CommonServiceHelper.outputService = outputService;
    }
}
