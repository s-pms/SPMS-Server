package cn.hamm.spms.module.channel.purchaseprice;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("purchasePrice")
@Description("采购价格")
public class PurchasePriceController extends BaseController<PurchasePriceEntity, PurchasePriceService, PurchasePriceRepository> implements IPurchasePriceAction {
    @Description("查询物料和供应商的价格")
    @RequestMapping("getByMaterialAndSupplier")
    @Filter(WhenGetDetail.class)
    public JsonData getByMaterialAndSupplier(@RequestBody @Validated(WhenGetByMaterialAndSupplier.class) PurchasePriceEntity entity) {
        return jsonData(service.getByMaterialAndSupplier(entity.getMaterial(), entity.getSupplier()));
    }
}
