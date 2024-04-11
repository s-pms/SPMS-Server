package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("salePrice")
@Description("销售价格")
public class SalePriceController extends BaseController<SalePriceEntity, SalePriceService, SalePriceRepository> {
    @Description("查询物料和客户的价格")
    @RequestMapping("getByMaterialAndCustomer")
    @Filter(RootEntity.WhenGetDetail.class)
    public JsonData getByMaterialAndCustomer(@RequestBody @Validated(SalePriceEntity.WhenGetByMaterialAndCustomer.class) SalePriceEntity entity) {
        return jsonData(service.getByMaterialAndCustomer(entity.getMaterial(), entity.getCustomer()));
    }
}
