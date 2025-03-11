package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.model.Sort;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_EDIT;
import static cn.hamm.airpower.exception.ServiceError.PARAM_INVALID;

/**
 * <h1>Service</h1>
 *
 * @author zfy
 */
@Service
public class RoutingService extends BaseService<RoutingEntity, RoutingRepository> {
    /**
     * <h3>排序字段</h3>
     */
    private static final String ORDER_FIELD_NAME = "orderNo";

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
        List<RoutingOperationEntity> details = Services.getRoutingOperationService().filter(filter, new Sort().setField(ORDER_FIELD_NAME).setDirection(Sort.ASC));
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
        PARAM_INVALID.whenNull(routing.getBom(), "请配置工艺使用的BOM");
        return routing;
    }

    @Override
    protected void beforePublish(@NotNull RoutingEntity routing) {
        FORBIDDEN_EDIT.when(CollectionUtils.isEmpty(routing.getDetails()), "发布失败，工艺没有任何工序流程");
    }
}
