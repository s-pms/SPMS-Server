package cn.hamm.spms.module.mes.routing.operation;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author zfy
 */
@RestController
@Description("工序配置")
@ApiController("routingOperation")
public class RoutingOperationController extends BaseController<RoutingOperationEntity, RoutingOperationService, RoutingOperationRepository> {
}