package cn.hamm.spms.base.bill.detail;

import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    /**
     * <h3>更新明细的数量</h3>
     *
     * @param billId      单据ID
     * @param quantity    本次更新数量
     * @param billService 单据Service
     * @param detailCheck 明细检查函数
     */
    public <B extends AbstractBaseBillEntity<B, ?>, BS extends AbstractBaseBillService<B, ?, ?, ?, ?>> void updateDetailQuantity(long billId, double quantity, @NotNull BS billService, Consumer<E> detailCheck) {
        List<E> details = getAllByBillId(billId);
        for (E detail : details) {
            if (quantity <= 0) {
                break;
            }
            try {
                detailCheck.accept(detail);
            } catch (Exception e) {
                continue;
            }
            if (detail.getIsFinished()) {
                continue;
            }

            // 还需要完成的数量
            double detailNeedQuantity = NumberUtil.subtract(detail.getQuantity(), detail.getFinishQuantity());
            if (quantity < detailNeedQuantity) {
                // 添加单据完成数量
                detail.setFinishQuantity(quantity);
            } else {
                quantity = NumberUtil.subtract(quantity, detailNeedQuantity);
                detail.setFinishQuantity(detail.getQuantity()).setIsFinished(true);
            }
            update(detail);
        }
        // 判断所有明细是否完成
        details = getAllByBillId(billId);
        boolean isAllFinished = details.stream().allMatch(BaseBillDetailEntity::getIsFinished);
        if (isAllFinished) {
            // 明细已全部完成
            billService.setBillDetailsAllFinished(billId);
        }
    }
}
