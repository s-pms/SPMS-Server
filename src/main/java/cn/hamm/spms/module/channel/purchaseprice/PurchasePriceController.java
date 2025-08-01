package cn.hamm.spms.module.channel.purchaseprice;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.api.fiter.Filter;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("purchasePrice")
@Description("采购价格")
public class PurchasePriceController extends BaseController<
        PurchasePriceEntity, PurchasePriceService, PurchasePriceRepository
        > implements IPurchasePriceAction {
    @Description("查询物料和供应商的价格")
    @PostMapping("getByMaterialAndSupplier")
    @Filter(WhenGetDetail.class)
    public Json getByMaterialAndSupplier(@RequestBody @Validated(WhenGetByMaterialAndSupplier.class) PurchasePriceEntity purchasePrice) {
        return Json.data(
                service.getByMaterialAndSupplier(
                        purchasePrice.getMaterial(),
                        purchasePrice.getSupplier()
                )
        );
    }
}
