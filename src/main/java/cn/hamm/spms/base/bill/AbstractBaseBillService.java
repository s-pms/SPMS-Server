package cn.hamm.spms.base.bill;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailService;
import org.jetbrains.annotations.NotNull;
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
 * @author Hamm.cn
 */
public abstract class AbstractBaseBillService<
        E extends AbstractBaseBillEntity<E, D>, R extends BaseRepository<E>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>, DR extends BaseBillDetailRepository<D>
        > extends BaseService<E, R> {

    @Autowired(required = false)
    protected DS detailService;


    /**
     * <h2>添加完成数量</h2>
     *
     * @param sourceDetail 提交明细
     */
    public final void addFinish(@NotNull D sourceDetail) {
        // 查保存的明细
        D savedDetail = detailService.get(sourceDetail.getId());
        // 更新保存明细的完成数量 = 保存明细的完成数量 + 提交的完成数量
        savedDetail.setFinishQuantity(savedDetail.getFinishQuantity() + sourceDetail.getQuantity());
        detailService.update(savedDetail);

        // 操作完成的后置方法
        afterAddDetailFinish(savedDetail.getId(), sourceDetail);

        // 开始判断是否整个单据数量已超标
        List<D> details = detailService.getAllByBillId(savedDetail.getBillId());
        boolean isAllFinished = true;
        for (D d : details) {
            if (d.getFinishQuantity() < d.getQuantity()) {
                isAllFinished = false;
                break;
            }
        }
        if (isAllFinished) {
            // 触发单据完成的后置方法
            afterAllDetailsFinished(savedDetail.getBillId());
        }
    }

    /**
     * <h2>添加完成数量前置方法</h2>
     *
     * @param sourceDetail 提交明细
     */
    protected void afterAddDetailFinish(long detailId, D sourceDetail) {
    }

    /**
     * <h2>所有明细数量全部完成后置方法</h2>
     *
     * @param id 单据ID
     */
    protected void afterAllDetailsFinished(Long id) {
    }

    /**
     * <h2>单据明细保存后置方法</h2>
     *
     * @param bill 单据
     */
    protected void afterDetailSaved(E bill) {
    }

    @Override
    protected E afterGet(@NotNull E bill) {
        List<D> details = detailService.getAllByBillId(bill.getId());
        bill.setDetails(details);
        return bill;
    }

    @Override
    protected void afterAdd(long id, @NotNull E source) {
        saveDetails(id, source.getDetails());
    }

    @Override
    protected void afterUpdate(long id, @NotNull E source) {
        saveDetails(id, source.getDetails());
    }

    /**
     * <h2>保存单据明细</h2>
     * <li>
     * 请不要再重写后直接调用 #{@link #update(RootEntity)} #{@link #updateWithNull(RootEntity)}，避免出现调用循环。
     * </li>
     * <li>
     * 如需再次保存，请调用 #{@link #updateToDatabase(RootEntity)} }
     * </li>
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
     * <h2>设置为已审核状态</h2>
     *
     * @param bill 单据
     */
    public final void setAudited(@NotNull E bill) {
        bill.setStatus(getAuditedStatus().getKey());
    }

    /**
     * <h2>设置为审核中状态</h2>
     *
     * @param bill 单据
     */
    public final void setAuditing(@NotNull E bill) {
        bill.setStatus(getAuditingStatus().getKey());
    }

    /**
     * <h2>单据是否可审核</h2>
     *
     * @param bill 单据
     * @return 是否审核
     */
    public final boolean canAudit(@NotNull E bill) {
        return getAuditedStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h2>单据是否可驳回</h2>
     *
     * @param bill 单据
     * @return 是否可驳回
     */
    public final boolean canReject(@NotNull E bill) {
        return getAuditedStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h2>设置单据为驳回状态</h2>
     *
     * @param bill 单据
     */
    public final void setReject(@NotNull E bill) {
        bill.setStatus(getRejectedStatus().getKey());
    }

    /**
     * <h2>单据是否可编辑</h2>
     *
     * @param bill 单据
     * @return 是否可编辑
     */
    public final boolean canEdit(@NotNull E bill) {
        return getRejectedStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h2>获取审核中状态</h2>
     *
     * @return 审核中状态
     */
    public abstract IDictionary getAuditingStatus();

    /**
     * <h2>获取已审核状态</h2>
     *
     * @return 已审核状态
     */
    public abstract IDictionary getAuditedStatus();

    /**
     * <h2>获取驳回状态</h2>
     *
     * @return 驳回状态
     */
    public abstract IDictionary getRejectedStatus();
}
