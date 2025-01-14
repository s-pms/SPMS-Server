package cn.hamm.spms.base.bill;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.util.TaskUtil;
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
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>,
        DR extends BaseBillDetailRepository<D>
        > extends BaseService<E, R> {

    @Autowired(required = false)
    protected DS detailService;

    /**
     * <h3>添加完成数量</h3>
     *
     * @param sourceDetail 提交明细
     */
    public void addFinish(@NotNull D sourceDetail) {
        // 查保存的明细
        D savedDetail = detailService.get(sourceDetail.getId());
        // 更新保存明细的完成数量 = 保存明细的完成数量 + 提交的完成数量
        savedDetail.setFinishQuantity(savedDetail.getFinishQuantity() + sourceDetail.getQuantity());
        detailService.update(savedDetail);

        // 操作完成的后置方法
        afterAddDetailFinish(savedDetail.getId(), sourceDetail);

        // 开始判断是否整个单据数量已超标
        List<D> details = detailService.getAllByBillId(savedDetail.getBillId());
        // 所有的完成数量都不小于计划数量 标识全部完成
        boolean isAllFinished = details.stream()
                .noneMatch(d -> d.getFinishQuantity() < d.getQuantity());
        if (isAllFinished) {
            // 触发单据完成的后置方法
            afterAllDetailsFinished(savedDetail.getBillId());
        }
    }

    /**
     * <h3>添加完成数量前置方法</h3>
     *
     * @param detailId     明细ID
     * @param sourceDetail 提交明细
     */
    protected void afterAddDetailFinish(long detailId, D sourceDetail) {
    }

    /**
     * <h3>所有明细数量全部完成后置方法</h3>
     *
     * @param id 单据ID
     */
    protected void afterAllDetailsFinished(Long id) {
    }

    /**
     * <h3>单据明细保存后置方法</h3>
     *
     * @param bill 单据
     */
    protected void afterDetailSaved(E bill) {
    }

    @Override
    protected E afterGet(@NotNull E bill) {
        List<D> details = detailService.getAllByBillId(bill.getId());
        return bill.setDetails(details);
    }

    @Override
    protected final void afterAdd(long id, @NotNull E source) {
        saveDetails(id, source.getDetails());
        TaskUtil.run(() -> afterBillAdd(id));
    }

    protected void afterBillAdd(long id) {

    }

    @Override
    protected void afterUpdate(long id, @NotNull E source) {
        saveDetails(id, source.getDetails());
    }

    /**
     * <h3>保存单据明细</h3>
     * <li>
     * 请不要再重写后直接调用 {@link #update(RootEntity)} #{@link #updateWithNull(RootEntity)}，避免出现调用循环。
     * </li>
     * <li>
     * 如需再次保存，请调用 {@link #updateToDatabase(RootEntity)} }
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

    protected void audit(long billId) {
        E bill = get(billId);
        ServiceError.FORBIDDEN.when(!canAudit(bill), "该单据状态无法审核");
        setAudited(bill);
        update(bill);
    }

    /**
     * <h3>设置为已审核状态</h3>
     *
     * @param bill 单据
     */
    public final void setAudited(@NotNull E bill) {
        bill.setStatus(getAuditedStatus().getKey());
    }

    /**
     * <h3>设置为审核中状态</h3>
     *
     * @param bill 单据
     */
    public final void setAuditing(@NotNull E bill) {
        bill.setStatus(getAuditingStatus().getKey());
    }

    /**
     * <h3>单据是否可审核</h3>
     *
     * @param bill 单据
     * @return 是否审核
     */
    public boolean canAudit(@NotNull E bill) {
        return getAuditingStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h3>单据是否可驳回</h3>
     *
     * @param bill 单据
     * @return 是否可驳回
     */
    public boolean canReject(@NotNull E bill) {
        return getAuditingStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h3>设置单据为驳回状态</h3>
     *
     * @param bill 单据
     */
    public final void setReject(@NotNull E bill) {
        bill.setStatus(getRejectedStatus().getKey());
    }

    /**
     * <h3>单据是否可编辑</h3>
     *
     * @param bill 单据
     * @return 是否可编辑
     */
    public boolean canEdit(@NotNull E bill) {
        return getRejectedStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h3>获取审核中状态</h3>
     *
     * @return 审核中状态
     */
    public abstract IDictionary getAuditingStatus();

    /**
     * <h3>获取已审核状态</h3>
     *
     * @return 已审核状态
     */
    public abstract IDictionary getAuditedStatus();

    /**
     * <h3>获取驳回状态</h3>
     *
     * @return 驳回状态
     */
    public abstract IDictionary getRejectedStatus();
}
