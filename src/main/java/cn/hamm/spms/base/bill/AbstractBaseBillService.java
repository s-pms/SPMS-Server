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
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.order.OrderStatus;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.system.config.ConfigService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

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
     * <h3>获取手动标记完成配置</h3>
     *
     * @return 配置标识
     */
    protected ConfigFlag getManualFinishConfigFlag() {
        return null;
    }

    /**
     * <h3>获取自动审核配置</h3>
     *
     * @return 配置标识
     */
    protected ConfigFlag getAutoAuditConfigFlag() {
        return null;
    }

    /**
     * <h3>标记单据完成</h3>
     *
     * @param billId 单据ID
     */
    public final void manualFinish(long billId) {
        ConfigService configService = Services.getConfigService();
        ConfigFlag configFlag = getManualFinishConfigFlag();
        ServiceError.FORBIDDEN.whenNull(configFlag, "标记完成失败，没有找到配置项");
        ConfigEntity config = configService.get(configFlag);
        ServiceError.FORBIDDEN.when(!config.booleanConfig(), "未开启手动标记完成");
        finish(billId);
    }

    /**
     * <h3>标记单据完成</h3>
     *
     * @param billId 单据ID
     */
    protected final void finish(long billId) {
        IDictionary finishedStatus = getFinishedStatus();
        ServiceError.FORBIDDEN.whenNull(finishedStatus, "标记完成失败，没有找到完成状态");

        E bill = get(billId);
        ServiceError.FORBIDDEN.when(OrderStatus.PRODUCING.notEqualsKey(bill.getStatus()), "该订单状态无法标记完成");
        bill.setStatus(finishedStatus.getKey());
        update(bill);

        afterBillFinished(bill.getId());
    }

    /**
     * <h3>添加完成数量</h3>
     *
     * @param sourceDetail 提交明细
     */
    public void addFinish(@NotNull D sourceDetail) {
        // 查保存的明细
        D savedDetail = detailService.get(sourceDetail.getId());
        // 更新保存明细的完成数量 = 保存明细的完成数量 + 提交的完成数量
        double finishQuantity = savedDetail.getFinishQuantity() + sourceDetail.getQuantity();
        // 如果完成数量 >= 数量 则标记明细完成
        savedDetail.setFinishQuantity(finishQuantity).setIsFinished(finishQuantity >= savedDetail.getQuantity());
        detailService.update(savedDetail);

        // 明细添加成功后置方法
        afterDetailFinishAdded(savedDetail.getId(), sourceDetail);

        // 开始判断是否整个单据数量已超标
        List<D> details = detailService.getAllByBillId(savedDetail.getBillId());
        // 判断所有明细是否完成
        boolean isAllFinished = details.stream()
                .allMatch(BaseBillDetailEntity::getIsFinished);
        if (!isAllFinished) {
            return;
        }
        IDictionary finishedStatus = getFinishedStatus();
        if (Objects.isNull(finishedStatus)) {
            return;
        }
        E bill = get(savedDetail.getBillId());
        bill.setStatus(finishedStatus.getKey());

        update(bill);
        // 触发单据完成的后置方法
        afterBillFinished(savedDetail.getBillId());
    }

    /**
     * <h3>添加完成数量成功后置</h3>
     *
     * @param detailId     明细ID
     * @param sourceDetail 提交明细
     */
    protected void afterDetailFinishAdded(long detailId, D sourceDetail) {
    }

    /**
     * <h3>单据完成的后置方法</h3>
     *
     * @param billId 单据ID
     */
    protected void afterBillFinished(Long billId) {
    }

    /**
     * <h3>单据明细保存后置方法</h3>
     *
     * @param bill 单据
     */
    protected void afterDetailSaved(E bill) {
    }

    @Override
    protected final E afterGet(@NotNull E bill) {
        List<D> details = detailService.getAllByBillId(bill.getId());
        return bill.setDetails(details);
    }

    @Override
    protected final void afterAdd(long id, @NotNull E source) {
        saveDetails(id, source.getDetails());
        ConfigFlag configFlag = getAutoAuditConfigFlag();
        if (Objects.nonNull(configFlag)) {
            ConfigEntity config = Services.getConfigService().get(configFlag);
            if (config.booleanConfig()) {
                audit(id);
            }
        }
        TaskUtil.run(() -> afterBillAdd(id));
    }

    /**
     * <h3>单据添加后置</h3>
     *
     * @param id 单据ID
     */
    protected void afterBillAdd(long id) {

    }

    @Override
    protected final void afterUpdate(long id, @NotNull E source) {
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

    /**
     * <h3>单据审核</h3>
     *
     * @param billId 单据ID
     */
    protected final void audit(long billId) {
        E bill = get(billId);
        ServiceError.FORBIDDEN.when(!canAudit(bill), "该单据状态无法审核");
        setAudited(bill);
        update(bill);
        TaskUtil.run(() -> afterBillAudited(bill.getId()));
    }

    /**
     * <h3>单据审核后置</h3>
     *
     * @param billId 单据ID
     */
    protected void afterBillAudited(long billId) {

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
    public final boolean canAudit(@NotNull E bill) {
        return getAuditingStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h3>单据是否可驳回</h3>
     *
     * @param bill 单据
     * @return 是否可驳回
     */
    public final boolean canReject(@NotNull E bill) {
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
    public final boolean canEdit(@NotNull E bill) {
        return getRejectedStatus().equalsKey(bill.getStatus());
    }

    /**
     * <h3>获取审核中状态</h3>
     *
     * @return 审核中状态
     */
    protected abstract IDictionary getAuditingStatus();

    /**
     * <h3>获取已审核状态</h3>
     *
     * @return 已审核状态
     */
    protected abstract IDictionary getAuditedStatus();

    /**
     * <h3>获取驳回状态</h3>
     *
     * @return 驳回状态
     */
    protected abstract IDictionary getRejectedStatus();

    /**
     * <h3>获取完成状态</h3>
     *
     * @return 完成状态
     */
    protected abstract IDictionary getFinishedStatus();
}
