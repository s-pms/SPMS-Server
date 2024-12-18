package cn.hamm.spms.base.bill.detail;

import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>单据明细Service基类</h1>
 *
 * @param <E> 明细实体
 * @param <R> 明细数据源
 * @author Hamm.cn
 */
public class BaseBillDetailService<E extends BaseBillDetailEntity<E>, R extends BaseBillDetailRepository<E>> extends BaseService<E, R> {

    /**
     * <h3>根据单据ID删除所有明细</h3>
     *
     * @param billId 单据ID
     */
    public final void deleteAllByBillId(Long billId) {
        List<E> details = getAllByBillId(billId);
        details.forEach(detail -> repository.deleteById(detail.getId()));
    }

    /**
     * <h3>查询指定单据的所有明细</h3>
     *
     * @param billId 单据ID
     * @return 明细
     */
    public final List<E> getAllByBillId(Long billId) {
        return repository.getAllByBillId(billId);
    }

    /**
     * <h3>保存指定单据的明细</h3>
     *
     * @param billId  单据ID
     * @param details 明细
     * @return 存储后的明细
     */
    public final @NotNull List<E> saveDetails(Long billId, @NotNull List<E> details) {
        deleteAllByBillId(billId);
        List<E> savedDetails = new ArrayList<>(details.size());
        details.forEach(detail -> {
            detail.setBillId(billId);
            long detailId = add(detail);
            savedDetails.add(get(detailId));
        });
        return savedDetails;
    }
}
