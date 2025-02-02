package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("order")
@Description("生产订单")
@Extends(exclude = Api.Delete)
public class OrderController extends BaseBillController<OrderEntity, OrderService, OrderRepository, OrderDetailEntity, OrderDetailService, OrderDetailRepository> {

    @Description("开始生产")
    @PostMapping("start")
    @Filter(WhenGetDetail.class)
    public Json start(@RequestBody @Validated(WhenIdRequired.class) OrderEntity order) {
        service.start(order.getId());
        return Json.success("开始生产成功");
    }

    @Description("暂停生产")
    @PostMapping("pause")
    @Filter(WhenGetDetail.class)
    public Json pause(@RequestBody @Validated(WhenIdRequired.class) OrderEntity order) {
        service.pause(order.getId());
        return Json.success("暂停生产成功");
    }
}
