package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.model.json.JsonData;
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
@RequestMapping("salePrice")
@Description("销售价格")
public class SalePriceController extends BaseController<SalePriceEntity, SalePriceService, SalePriceRepository> implements ISalePriceAction {
    @Description("查询物料和客户的价格")
    @RequestMapping("getByMaterialAndCustomer")
    @Filter(WhenGetDetail.class)
    public JsonData getByMaterialAndCustomer(@RequestBody @Validated(WhenGetByMaterialAndCustomer.class) SalePriceEntity entity) {
        return jsonData(service.getByMaterialAndCustomer(entity.getMaterial(), entity.getCustomer()));
    }
}
