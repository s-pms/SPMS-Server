package cn.hamm.spms.module.channel.purchaseprice;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("purchasePrice")
@Description("采购价格")
public class PurchasePriceController extends BaseController<PurchasePriceEntity, PurchasePriceService, PurchasePriceRepository> implements IPurchasePriceAction {
    @Description("查询物料和供应商的价格")
    @RequestMapping("getByMaterialAndSupplier")
    @Filter(WhenGetDetail.class)
    public Json getByMaterialAndSupplier(@RequestBody @Validated(WhenGetByMaterialAndSupplier.class) PurchasePriceEntity entity) {
        return Json.data(service.getByMaterialAndSupplier(entity.getMaterial(), entity.getSupplier()));
    }
}
