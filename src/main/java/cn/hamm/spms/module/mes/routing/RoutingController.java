package cn.hamm.spms.module.mes.routing;

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
@Description("生产工艺")
@ApiController("routing")
public class RoutingController extends BaseController<RoutingEntity, RoutingService, RoutingRepository> {
}
