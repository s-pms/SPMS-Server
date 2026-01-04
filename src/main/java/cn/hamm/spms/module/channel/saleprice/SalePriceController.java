package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Json;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("salePrice")
@Description("销售价格")
public class SalePriceController extends BaseController<SalePriceEntity, SalePriceService, SalePriceRepository> implements ISalePriceAction {
    @Description("查询物料和客户的价格")
    @PostMapping("getByMaterialAndCustomer")
    public Json getByMaterialAndCustomer(@RequestBody @Validated(WhenGetByMaterialAndCustomer.class) SalePriceEntity salePrice) {
        return Json.data(
                service.getByMaterialAndCustomer(
                        salePrice.getMaterial(), salePrice.getCustomer()
                )
        );
    }
}
