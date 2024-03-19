package cn.hamm.spms.base.bill;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * <h1>单据明细的标准接口</h1>
 *
 * @param <E> 单据实体
 * @param <D> 明细实体
 * @author Hamm
 */
@NoRepositoryBean
public interface BaseBillRepository<E extends AbstractBaseBillEntity<E, D>, D extends BaseBillDetailEntity<D>> extends BaseRepository<E> {
}
