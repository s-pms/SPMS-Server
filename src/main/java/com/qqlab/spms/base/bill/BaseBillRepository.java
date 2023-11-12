package com.qqlab.spms.base.bill;

import com.qqlab.spms.base.BaseRepository;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * <h1>单据明细的标准接口</h1>
 *
 * @param <B> 单据实体
 * @param <D> 明细实体
 * @author hamm
 */
@NoRepositoryBean
public interface BaseBillRepository<B extends BaseBillEntity<B, D>, D extends BaseBillDetailEntity<D>> extends BaseRepository<B> {
}
