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
     * 走 {@code BaseService.deleteAll}：它逐条触发 {@code beforeDelete} /
     * {@code afterDelete} 钩子，工序上挂着 {@code bom} 等关联，
     * 删除前的校验与级联都要靠钩子。
     * </p>
     * <p>
     * <b>不能写成 {@code forEach(this::delete)}</b>：
     * {@code delete(long id)} 第一步是 {@code get(id)}，而
     * {@code getById} 第一行是 {@code entityManager.clear()}，
     * 会把上一条已标记的删除冲掉 —— 实测「批量删除只有最后一条生效」，
     * 表现为工艺每改一次就多留一批工序。
     * </p>
     * <p>
     * 不使用 {@code repository.deleteAll} + {@code flush}：那是直接拼的批量 SQL，
     * <b>不触发</b> JPA 实体生命周期回调、<b>不走</b> 业务钩子，也不做级联 ——
     * 被别的单据引用的工序会被静默删掉，留下悬空引用。
     * </p>
     * <p>
     * 本方法在 {@code RoutingService.afterAppUpdate} 里被调用，即每次编辑工艺都会触发。
     * 修复前因批量删除不生效，表现为：工艺改一次，工序就多留一批，
     * 生产现场会出现同一道工序重复好几行。
     * </p>
     *
     * @param id 工艺 ID
     */
    public void deleteByRoutingId(long id) {
        List<RoutingOperationEntity> exists = filter(new RoutingOperationEntity().setRoutingId(id));
        // 走 service.delete 保证前后置钩子被触发
        exists.forEach(entity -> delete(entity.getId()));
        log.info("工艺 {} 的 {} 条工序已删除", id, exists.size());
    }
}
