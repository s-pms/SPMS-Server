package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.system.config.ConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;

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

    @Description("订单报工")
    @PostMapping("addOrderDetail")
    @Filter(WhenGetDetail.class)
    public Json addOrderDetail(@RequestBody @Validated(WhenAddFinish.class) OrderDetailEntity orderDetail) {
        service.addOrderDetail(orderDetail);
        return Json.success("提交订单报工成功");
    }

    @Description("手动设置为生产完成待入库状态")
    @PostMapping("setFinishToInput")
    @Filter(WhenGetDetail.class)
    public Json setFinishToInput(@RequestBody @Validated(WhenIdRequired.class) OrderEntity order) {
        ConfigService configService = Services.getConfigService();
        ConfigEntity config = configService.get(ConfigFlag.ORDER_MANUAL_FINISH);
        FORBIDDEN.when(!config.booleanConfig(), "未开启手动标记订单生产完成");
        service.setBillDetailsAllFinished(order.getId());
        return Json.success("手动设置为生产完成待入库状态成功");
    }
}
