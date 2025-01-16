package cn.hamm.spms.module.mes.routing.operation;

import cn.hamm.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author zfy
 */
@Service
public class RoutingOperationService extends BaseService<RoutingOperationEntity, RoutingOperationRepository> {

    public void deleteByRoutingId(long id) {
        List<RoutingOperationEntity> exists = filter(new RoutingOperationEntity().setRoutingId(id));
        for (RoutingOperationEntity exist : exists) {
            delete(exist.getId());
        }
    }
}
