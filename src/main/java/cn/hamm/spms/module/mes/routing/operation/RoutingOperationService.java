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

    /**
     * 删除某个工艺下的全部工序
     * <p>
     * 必须走 {@code repository.deleteAll} + {@code flush}，不能写成
     * {@code list.stream().mapToLong(CurdEntity::getId).forEach(this::delete)}。
     * <p>
     * 后者会被框架的 flush 时机坑掉：{@code CurdService.delete(long)} 走的是
     * {@code TransactionHelper.run(Function)}，而这个重载<b>没有</b> {@code @Transactional}
     * （只有 {@code run(Supplier)} 有）。于是循环里每删一条都会在下一条 {@code get(id)} 的
     * auto-flush 时被刷掉，最后一条永远停留在持久化上下文里，直到进程退出才丢弃 ——
     * 实际表现是<b>批量删除只有最后一条生效</b>。
     * <p>
     * 这个方法在 {@code RoutingService.afterAppUpdate} 里被调用，即每次编辑工艺都会触发。
     * 修复前表现为：工艺改一次，工序就多留一批，生产现场会出现同一道工序重复好几行。
     *
     * @param id 工艺 ID
     */
    public void deleteByRoutingId(long id) {
        List<RoutingOperationEntity> exists = filter(new RoutingOperationEntity().setRoutingId(id));
        if (exists.isEmpty()) {
            return;
        }
        repository.deleteAll(exists);
        // 显式刷库，确保删除在后续新增之前落库
        repository.flush();
    }
}
