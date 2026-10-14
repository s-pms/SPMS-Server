package cn.hamm.spms.base.bill.detail;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * <h1>单据明细数据源基接口</h1>
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
     * @return 明细列表
     * @apiNote 不要加 {@code @Lock}：本方法有事务外的调用点，而 {@code SELECT ... FOR UPDATE}
     * 必须在事务内，否则报 {@code Query requires transaction be in progress}。并发控制请加在单据行上，
     * 见 {@code AbstractBaseBillService#getBillForUpdate(long)}
     */
    List<E> getAllByBillId(Long billId);
}
