package cn.hamm.spms.module.channel;

import cn.hamm.spms.module.channel.customer.CustomerService;
import cn.hamm.spms.module.channel.purchase.PurchaseService;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailService;
import cn.hamm.spms.module.channel.purchaseprice.PurchasePriceService;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import cn.hamm.spms.module.channel.saleprice.SalePriceService;
import cn.hamm.spms.module.channel.supplier.SupplierService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class ChannelServices {
    @Getter
    private static CustomerService customerService;

    @Getter
    private static PurchaseService purchaseService;

    @Getter
    private static PurchaseDetailService purchaseDetailService;

    @Getter
    private static PurchasePriceService purchasePriceService;

    @Getter
    private static SaleService saleService;

    @Getter
    private static SaleDetailService saleDetailService;

    @Getter
    private static SalePriceService salePriceService;

    @Getter
    private static SupplierService supplierService;

    @Autowired
    private void initService(
            CustomerService customerService,
            PurchaseService purchaseService,
            PurchaseDetailService purchaseDetailService,
            PurchasePriceService purchasePriceService,
            SaleService saleService,
            SaleDetailService saleDetailService,
            SalePriceService salePriceService,
            SupplierService supplierService
    ) {
        ChannelServices.customerService = customerService;
        ChannelServices.purchaseService = purchaseService;
        ChannelServices.purchaseDetailService = purchaseDetailService;
        ChannelServices.purchasePriceService = purchasePriceService;
        ChannelServices.saleService = saleService;
        ChannelServices.saleDetailService = saleDetailService;
        ChannelServices.salePriceService = salePriceService;
        ChannelServices.supplierService = supplierService;
    }
}
