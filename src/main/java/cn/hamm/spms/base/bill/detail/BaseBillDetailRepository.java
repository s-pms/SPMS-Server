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
     * <p>
     * 刻意<b>不加</b> {@code @Lock}：这个方法有事务外的调用点（接口返回明细列表），
     * 而 {@code SELECT ... FOR UPDATE} 要求必须在事务里，否则报
     * {@code Query requires transaction be in progress}。
     * </p>
     * <p>
     * 需要「读到最新已提交数据」的场景（判断明细是否全部完成，修复 P2-4），
     * 用 {@code BaseBillDetailService.isAllDetailFinished}，
     * 它内部逐行走 {@code getForUpdate} 做当前读。
     * </p>
     * <p>
     * 用 Spring Data 的<b>方法名派生</b>而不是 {@code @Query}：
     * 本接口是泛型基类，实际实体是 {@code PurchaseDetailEntity} 等具体子类，
     * JPQL 里的实体名必须是具体实体名，泛型基类里拿不到。
     * 派生查询由 Spring Data 按实际实体生成，对泛型天然友好。
     * </p>
     *
     * @param billId 单据 ID
     * @return 明细
     */
    List<E> getAllByBillId(Long billId);
}
