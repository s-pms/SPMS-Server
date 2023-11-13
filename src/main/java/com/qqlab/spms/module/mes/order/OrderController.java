package com.qqlab.spms.module.mes.order;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.mes.order.detail.OrderDetailEntity;
import com.qqlab.spms.module.mes.order.detail.OrderDetailRepository;
import com.qqlab.spms.module.mes.order.detail.OrderDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("order")
@Description("生产订单")
@Extends(exclude = Api.Delete)
public class OrderController extends BaseBillController<OrderEntity, OrderService, OrderRepository, OrderDetailEntity, OrderDetailService, OrderDetailRepository> {
}
