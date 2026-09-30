package cn.hamm.spms.module.mes.routing.operation;

import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author zfy
 */
@Slf4j
@Service
public class RoutingOperationService extends BaseService<RoutingOperationEntity, RoutingOperationRepository> {

    /**
     * 删除某个工艺下的全部工序
     * <p>
     * 逐条走 {@code delete(id)} 以触发钩子：工序上挂着 bom 等关联，
     * 校验与级联都靠钩子，批量 SQL 会静默留下悬空引用。
     * </p>
     *
     * @param id 工艺 ID
     */
    public void deleteByRoutingId(long id) {
        List<RoutingOperationEntity> exists = filter(new RoutingOperationEntity().setRoutingId(id));
        exists.forEach(entity -> delete(entity.getId()));
        log.info("工艺 {} 的 {} 条工序已删除", id, exists.size());
    }
}
