package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author zfy
 */
@RestController
@Description("生产工艺")
@Api("routing")
public class RoutingController extends BaseController<RoutingEntity, RoutingService, RoutingRepository> {
}
