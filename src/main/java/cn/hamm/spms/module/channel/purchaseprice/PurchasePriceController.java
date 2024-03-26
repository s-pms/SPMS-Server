package cn.hamm.spms.module.channel.purchaseprice;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("purchasePrice")
@Description("采购价格")
public class PurchasePriceController extends BaseController<PurchasePriceEntity, PurchasePriceService, PurchasePriceRepository> {
    @Description("查询物料和供应商的价格")
    @PostMapping("getByMaterialAndSupplier")
    @Filter(RootEntity.WhenGetDetail.class)
    public JsonData getByMaterialAndSupplier(@RequestBody @Validated(PurchasePriceEntity.WhenGetByMaterialAndSupplier.class) PurchasePriceEntity entity) {
        return jsonData(service.getByMaterialAndSupplier(entity.getMaterial(), entity.getSupplier()));
    }
}
