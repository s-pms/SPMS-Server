package cn.hamm.spms.module.channel.saleprice;

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
@ApiController("salePrice")
@Description("销售价格")
public class SalePriceController extends BaseController<SalePriceEntity, SalePriceService, SalePriceRepository> implements ISalePriceAction {
    @Description("查询物料和客户的价格")
    @RequestMapping("getByMaterialAndCustomer")
    @Filter(WhenGetDetail.class)
    public Json getByMaterialAndCustomer(@RequestBody @Validated(WhenGetByMaterialAndCustomer.class) SalePriceEntity entity) {
        return Json.data(service.getByMaterialAndCustomer(entity.getMaterial(), entity.getCustomer()));
    }
}
