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
    @Description("完成")
    @PostMapping("finish")
    @Filter(WhenGetDetail.class)
    public Json finish(@RequestBody @Validated(WhenIdRequired.class) OrderEntity order) {
        service.finish(order.getId());
        return Json.success("标记完成成功");
    }
}
