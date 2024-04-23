package cn.hamm.spms.common;

import cn.hamm.spms.module.channel.purchase.PurchaseService;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import cn.hamm.spms.module.wms.input.InputService;
import cn.hamm.spms.module.wms.output.OutputService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
@SuppressWarnings("UnusedReturnValue")
public class Services {
    @Getter
    private static PurchaseService purchaseService;

    @Getter
    private static SaleService saleService;

    @Getter
    private static InputService inputService;

    @Getter
    private static OutputService outputService;

    @Getter
    private static CodeRuleService codeRuleService;

    @Autowired
    private void initService(
            CodeRuleService codeRuleService,
            PurchaseService purchaseService,
            SaleService saleService,
            InputService inputService,
            OutputService outputService
    ) {
        Services.codeRuleService = codeRuleService;
        Services.purchaseService = purchaseService;
        Services.saleService = saleService;
        Services.inputService = inputService;
        Services.outputService = outputService;
    }
}
