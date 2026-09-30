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
     * 不能加 {@code @Lock}：本方法有事务外的调用点，而
     * {@code SELECT ... FOR UPDATE} 必须在事务内，否则报
     * {@code Query requires transaction be in progress}。
     * </p>
     * <p>
     * 用方法名派生而非 {@code @Query}：本接口是泛型基类，JPQL 需要具体实体名，
     * 泛型里拿不到，派生查询由 Spring Data 按实际实体生成。
     * </p>
     *
     * @param billId 单据 ID
     * @return 明细
     */
    List<E> getAllByBillId(Long billId);
}
