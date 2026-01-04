package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.airpower.web.curd.Curd;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigService;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("order")
@Description("生产订单")
@Extends(exclude = Curd.Delete)
public class OrderController extends BaseBillController<OrderEntity, OrderService, OrderRepository, OrderDetailEntity, OrderDetailService, OrderDetailRepository> {

    @Description("开始生产")
    @PostMapping("start")
    public Json start(@RequestBody @Validated(WhenIdRequired.class) OrderEntity order) {
        service.start(order.getId());
        return Json.success("开始生产成功");
    }

    @Description("暂停生产")
    @PostMapping("pause")
    public Json pause(@RequestBody @Validated(WhenIdRequired.class) OrderEntity order) {
        service.pause(order.getId());
        return Json.success("暂停生产成功");
    }

    @Description("订单报工")
    @PostMapping("addOrderDetail")
    public Json addOrderDetail(@RequestBody @Validated(WhenAddFinish.class) OrderDetailEntity orderDetail) {
        service.addOrderDetail(orderDetail);
        return Json.success("提交订单报工成功");
    }

    @Description("手动设置为生产完成待入库状态")
    @PostMapping("setBillDetailsAllFinished")
    public Json setBillDetailsAllFinished(@RequestBody @Validated(WhenIdRequired.class) OrderEntity order) {
        ConfigService configService = Services.getConfigService();
        ConfigEntity config = configService.get(ConfigFlag.ORDER_MANUAL_FINISH);
        FORBIDDEN.when(!config.booleanConfig(), "未开启手动标记订单生产完成");
        service.setBillDetailsAllFinished(order.getId());
        return Json.success("手动设置为生产完成待入库状态成功");
    }
}
