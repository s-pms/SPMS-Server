package cn.hamm.spms.base.bill;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <h1>单据Service基类</h1>
 *
 * @param <E>   单据实体
 * @param <R>   单据数据源
 * @param <D>   明细实体
 * @param <DS>  明细Service
 * @param <DR>> 明细数据源
 * @author Hamm
 */
public abstract class AbstractBaseBillService<
        E extends AbstractBaseBillEntity<E, D>, R extends BaseRepository<E>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>, DR extends BaseBillDetailRepository<D>
        > extends BaseService<E, R> {

    @Autowired(required = false)
    protected DS detailService;


    /**
     * 添加完成数量
     *
     * @param sourceDetail 提交明细
     */
    public final void addFinish(D sourceDetail) {
        sourceDetail = beforeAddFinish(sourceDetail);
        D savedDetail = detailService.get(sourceDetail.getId());
        sourceDetail.setFinishQuantity(savedDetail.getFinishQuantity() + sourceDetail.getQuantity());
        detailService.update(sourceDetail);
        List<D> details = detailService.getAllByBillId(sourceDetail.getBillId());
        boolean isAllFinished = true;
        for (D d : details) {
            if (d.getFinishQuantity() < d.getQuantity()) {
                isAllFinished = false;
                break;
            }
        }
        if (isAllFinished) {
            afterAllDetailsFinished(sourceDetail.getBillId());
        }
    }

    /**
     * 添加完成数量前置方法
     *
     * @param sourceDetail 提交明细
     * @return 提交明细
     */
    protected D beforeAddFinish(D sourceDetail) {
        return sourceDetail;
    }

    /**
     * 所有明细数量全部完成后置方法
     *
     * @param id 单据ID
     */
    protected void afterAllDetailsFinished(Long id) {
    }

    /**
     * 单据明细保存后置方法
     *
     * @param bill 单据
     */
    protected void afterDetailSaved(E bill) {
    }

    @Override
    protected E afterGet(E bill) {
        List<D> details = detailService.getAllByBillId(bill.getId());
        bill.setDetails(details);
        return bill;
    }

    @Override
    protected void afterAdd(long id, E source) {
        saveDetails(id, source.getDetails());
    }

    @Override
    protected void afterUpdate(long id, E source) {
        saveDetails(id, source.getDetails());
    }

    /**
     * 保存单据明细
     *
     * @param billId  单据ID
     * @param details 明细列表
     */
    private void saveDetails(long billId, List<D> details) {
        E bill = get(billId);
        details = detailService.saveDetails(bill.getId(), details);
        bill.setDetails(details);
        afterDetailSaved(bill);
    }

    /**
     * 设置为已审核状态
     *
     * @param bill 单据
     * @return 实体
     */
    public abstract E setAudited(E bill);

    /**
     * 设置为审核中状态
     *
     * @param bill 单据
     * @return 实体
     */
    public abstract E setAuditing(E bill);

    /**
     * 单据是否已审核
     *
     * @param bill 单据
     * @return 是否审核
     */
    public abstract boolean isAudited(E bill);

    /**
     * 单据是否可驳回
     *
     * @param bill 单据
     * @return 是否可驳回
     */
    public abstract boolean canReject(E bill);

    /**
     * 设置单据为驳回状态
     *
     * @param bill 单据
     * @return 单据
     */
    public abstract E setReject(E bill);

    /**
     * 单据是否可编辑
     *
     * @param bill 单据
     * @return 是否可编辑
     */
    public abstract boolean canEdit(E bill);
}
