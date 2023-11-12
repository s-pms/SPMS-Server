package com.qqlab.spms.base.bill.detail;

import com.qqlab.spms.base.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * <h1>单据明细的标准接口</h1>
 *
 * @param <D> 明细实体
 * @author hamm
 */
@NoRepositoryBean
public interface BaseBillDetailRepository<D extends BaseBillDetailEntity<D>> extends BaseRepository<D> {
    /**
     * 根据单据ID查询所有明细
     *
     * @param billId 单据ID
     * @return 明细
     */
    List<D> getAllByBillId(Long billId);
}
