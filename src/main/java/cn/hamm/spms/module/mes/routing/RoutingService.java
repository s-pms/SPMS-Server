package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.model.Sort;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author zfy
 */
@Service
public class RoutingService extends BaseService<RoutingEntity, RoutingRepository> {
    @Override
    protected void afterUpdate(long id, @NotNull RoutingEntity source) {
        Services.getRoutingOperationService().deleteByRoutingId(id);
        List<RoutingOperationEntity> routingOperationList = source.getDetails();
        for (RoutingOperationEntity routingOperation : routingOperationList) {
            routingOperation.setRoutingId(id);
            Services.getRoutingOperationService().add(routingOperation);
        }
    }

    @Override
    protected RoutingEntity afterGet(@NotNull RoutingEntity routing) {
        RoutingOperationEntity filter = new RoutingOperationEntity().setRoutingId(routing.getId());
        List<RoutingOperationEntity> details = Services.getRoutingOperationService().filter(filter, new Sort().setField("orderNo").setDirection("asc"));
        routing.setDetails(details);
        return routing;
    }
}
