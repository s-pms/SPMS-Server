package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.curd.Sort;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationService;
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
     * 排序字段
     */
    private static final String ORDER_FIELD_NAME = "orderNo";

    @Override
    protected void afterAppUpdate(long id, @NotNull RoutingEntity source) {
        Services.getRoutingOperationService().deleteByRoutingId(id);
        afterAdd(id, source);
    }

    @Override
    protected void afterAppAdd(long id, @NotNull RoutingEntity source) {
        List<RoutingOperationEntity> routingOperationList = source.getDetails();
        RoutingOperationService routingOperationService = Services.getRoutingOperationService();
        for (RoutingOperationEntity routingOperation : routingOperationList) {
            routingOperation.setRoutingId(id);
            routingOperationService.add(routingOperation);
        }
    }

    @Override
    protected RoutingEntity afterAppGet(@NotNull RoutingEntity routing) {
        RoutingOperationEntity filter = new RoutingOperationEntity().setRoutingId(routing.getId());
        List<RoutingOperationEntity> details = Services.getRoutingOperationService().filter(filter, new Sort()
                .setField(ORDER_FIELD_NAME)
                .setDirection(Sort.ASC)
        );
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
        PARAM_INVALID.whenNull(routing.getBom(), "请配置工艺使用的 BOM");
        return routing;
    }

    @Override
    protected void beforePublish(@NotNull RoutingEntity routing) {
        FORBIDDEN_EDIT.when(CollectionUtils.isEmpty(routing.getDetails()), "发布失败，工艺没有任何工序流程");
    }
}
