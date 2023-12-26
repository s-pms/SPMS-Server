package com.qqlab.spms.base.bill.detail;

import com.qqlab.spms.base.BaseService;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>单据明细Service基类</h1>
 *
 * @param <E> 明细实体
 * @param <R> 明细数据源
 * @author Hamm
 */
public class BaseBillDetailService<E extends BaseBillDetailEntity<E>, R extends BaseBillDetailRepository<E>> extends BaseService<E, R> {

    /**
     * 根据单据ID删除所有明细
     *
     * @param billId 单据ID
     */
    public void deleteAllByBillId(Long billId) {
        List<E> details = getAllByBillId(billId);
        for (E detail : details) {
            repository.deleteById(detail.getId());
        }
    }

    /**
     * 查询指定单据的所有明细
     *
     * @param billId 单据ID
     * @return 明细
     */
    public List<E> getAllByBillId(Long billId) {
        return repository.getAllByBillId(billId);
    }

    /**
     * 保存指定单据的明细
     *
     * @param billId  单据ID
     * @param details 明细
     * @return 存储后的明细
     */
    public List<E> saveDetails(Long billId, List<E> details) {
        deleteAllByBillId(billId);
        List<E> savedDetails = new ArrayList<>(details.size());
        deleteAllByBillId(billId);
        for (E detail : details) {
            detail.setBillId(billId);
            savedDetails.add(repository.save(detail));
        }
        return savedDetails;
    }
}
