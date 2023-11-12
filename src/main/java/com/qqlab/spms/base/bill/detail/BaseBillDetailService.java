package com.qqlab.spms.base.bill.detail;

import com.qqlab.spms.base.BaseService;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>单据明细Service基类</h1>
 *
 * @param <D>  明细实体
 * @param <DR> 明细数据源
 * @author hamm
 */
public class BaseBillDetailService<D extends BaseBillDetailEntity<D>, DR extends BaseBillDetailRepository<D>> extends BaseService<D, DR> {

    /**
     * <h2>根据单据ID删除所有明细</h2>
     *
     * @param billId 单据ID
     */
    public void deleteAllByBillId(Long billId) {
        List<D> details = getAllByBillId(billId);
        for (D detail : details) {
            repository.deleteById(detail.getId());
        }
    }

    /**
     * <h2>查询指定单据的所有明细</h2>
     *
     * @param billId 单据ID
     * @return 明细
     */
    public List<D> getAllByBillId(Long billId) {
        return repository.getAllByBillId(billId);
    }

    /**
     * <h2>保存指定单据的明细</h2>
     *
     * @param billId  单据ID
     * @param details 明细
     * @return 存储后的明细
     */
    public List<D> saveDetails(Long billId, List<D> details) {
        deleteAllByBillId(billId);
        List<D> savedDetails = new ArrayList<>(details.size());
        deleteAllByBillId(billId);
        for (D detail : details) {
            detail.setBillId(billId);
            savedDetails.add(repository.save(detail));
        }
        return savedDetails;
    }
}
