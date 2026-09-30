package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.curd.model.query.Sort;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.mes.MesServices;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_EDIT;
import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>生产工艺</h1>
 *
 * @author zfy
 */
@Service
public class RoutingService extends BaseService<RoutingEntity, RoutingRepository> {
    /**
     * 排序字段
     */
    private static final String ORDER_FIELD_NAME = "orderNo";

    /**
     * 更新工艺时整体覆盖工序配置
     *
     * @param id     工艺 ID
     * @param source 工艺
     * @apiNote 工序是 {@code @Transient} 明细，只能整份重建：先按 routingId 全删再全建
     */
    @Override
    protected void afterAppUpdate(long id, @NotNull RoutingEntity source) {
        MesServices.getRoutingOperationService().deleteByRoutingId(id);
        afterAdd(id, source);
    }

    @Override
    protected void afterAppAdd(long id, @NotNull RoutingEntity source) {
        List<RoutingOperationEntity> routingOperationList = source.getDetails();
        RoutingOperationService routingOperationService = MesServices.getRoutingOperationService();
        for (RoutingOperationEntity routingOperation : routingOperationList) {
            routingOperation.setRoutingId(id);
            routingOperationService.add(routingOperation);
        }
    }

    /**
     * 按 {@code orderNo} 升序装配工序配置
     *
     * @param routing 工艺
     * @return 装配后的工艺
     */
    @Override
    protected RoutingEntity afterAppGet(@NotNull RoutingEntity routing) {
        RoutingOperationEntity filter = new RoutingOperationEntity().setRoutingId(routing.getId());
        List<RoutingOperationEntity> details = MesServices.getRoutingOperationService().filter(filter, new Sort()
                .setField(ORDER_FIELD_NAME)
                .setDirection(Sort.ASC)
        );
        routing.setDetails(details);
        return routing;
    }

    /**
     * 勾选「使用工艺 BOM」时强制要求关联 BOM，未勾选则清空关联
     *
     * @param routing 工艺
     * @return 处理后的工艺
     */
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
