package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.model.Sort;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author zfy
 */
@Service
public class RoutingService extends BaseService<RoutingEntity, RoutingRepository> {
    @Override
    protected void afterUpdate(long id, @NotNull RoutingEntity routing) {
        Services.getRoutingOperationService().deleteByRoutingId(id);
        afterAdd(id, routing);
    }

    @Override
    protected void afterAdd(long id, @NotNull RoutingEntity routing) {
        List<RoutingOperationEntity> routingOperationList = routing.getDetails();
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

    @Override
    protected RoutingEntity beforeAppSaveToDatabase(@NotNull RoutingEntity routing) {
        if (Objects.isNull(routing.getIsRoutingBom())) {
            return routing;
        }
        if (!routing.getIsRoutingBom()) {
            routing.setBom(null);
            return routing;
        }
        ServiceError.PARAM_INVALID.whenNull(routing.getBom(), "请配置工艺使用的BOM");
        return routing;
    }

    @Override
    protected void beforePublish(@NotNull RoutingEntity entity) {
        ServiceError.FORBIDDEN_EDIT.when(CollectionUtils.isEmpty(entity.getDetails()), "发布失败，工艺没有任何工序流程");
    }
}
