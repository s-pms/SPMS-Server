package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;
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
    @Override
    protected RoutingEntity beforePublish(@NotNull RoutingEntity routing) {
        RoutingEntity exist = service.get(routing.getId());
        ServiceError.FORBIDDEN_EDIT.when(CollectionUtils.isEmpty(exist.getDetails()), "发布失败，工艺没有任何工序流程");
        return routing;
    }
}
