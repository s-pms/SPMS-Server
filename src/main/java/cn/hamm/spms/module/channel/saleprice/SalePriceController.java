package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Filter;
import cn.hamm.airpower.core.model.Json;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("salePrice")
@Description("销售价格")
public class SalePriceController extends BaseController<SalePriceEntity, SalePriceService, SalePriceRepository> implements ISalePriceAction {
    @Description("查询物料和客户的价格")
    @PostMapping("getByMaterialAndCustomer")
    @Filter(WhenGetDetail.class)
    public Json getByMaterialAndCustomer(@RequestBody @Validated(WhenGetByMaterialAndCustomer.class) SalePriceEntity salePrice) {
        return Json.data(service.getByMaterialAndCustomer(salePrice.getMaterial(), salePrice.getCustomer()));
    }
}
