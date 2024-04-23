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
     * <h2>根据单据ID查询所有明细</h2>
     *
     * @param billId 单据ID
     * @return 明细
     */
    List<E> getAllByBillId(Long billId);
}
