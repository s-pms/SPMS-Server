package cn.hamm.spms.base.bill.detail;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * <h1>单据明细的标准接口</h1>
 *
 * @param <E> 明细实体
 * @author Hamm.cn
 */
@NoRepositoryBean
public interface BaseBillDetailRepository<E extends BaseBillDetailEntity<E>> extends BaseRepository<E> {
    /**
     * 根据单据 ID 查询所有明细
     *
     * @param billId 单据 ID
     * @return 明细
     */
    List<E> getAllByBillId(Long billId);

    /**
     * 根据单据 ID 一次性删除所有明细
     * <p>
     * 逐条 {@code deleteById} 会产生 N 条 DELETE。500 行的生产订单被驳回后重编辑
     * 就要串行执行 1000 条，单次请求持有行锁数秒，高并发下打满连接池。
     * </p>
     * <p>
     * 这里用 Spring Data 的<b>方法名派生</b>而不是 {@code @Query}：
     * 本接口是泛型基类，实际实体是 {@code PurchaseDetailEntity} 等具体子类，
     * JPQL 里写死 {@code BaseBillDetailEntity} 会在启动时报
     * {@code Validation failed for query}（JPQL 的实体名必须是具体实体名，
     * 泛型基类里拿不到）。派生查询由 Spring Data 按实际实体生成，对泛型天然友好。
     * </p>
     * <p>
     * ⚠️ 派生 delete 同样<b>不触发</b> JPA 实体生命周期回调，也不做级联。
     * 当前各单据明细没有需要级联清理的关联，因此可用；将来若给明细加上
     * 需要级联的关联，必须改回逐条删除。
     * </p>
     *
     * @param billId 单据 ID
     * @return 删除条数
     */
    long deleteByBillId(Long billId);
}
