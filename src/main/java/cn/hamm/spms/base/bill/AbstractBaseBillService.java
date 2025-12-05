package cn.hamm.spms.base.bill;

import cn.hamm.airpower.curd.CurdEntity;
import cn.hamm.airpower.dictionary.IDictionary;
import cn.hamm.airpower.helper.TransactionHelper;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.airpower.util.TaskUtil;
import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;

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
@Slf4j
public abstract class AbstractBaseBillService<
        E extends AbstractBaseBillEntity<E, D>, R extends BaseRepository<E>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>,
        DR extends BaseBillDetailRepository<D>
        > extends BaseService<E, R> {

    @Autowired(required = false)
    protected DS detailService;

    @Autowired
    protected TransactionHelper transactionHelper;

    /**
     * 获取自动审核配置
     *
     * @return 配置标识
     */
    protected ConfigFlag getAutoAuditConfigFlag() {
        log.info("获取自动审核配置, 无需自动审核");
        return null;
    }

    /**
     * 设置单据所有明细都已完成
     *
     * @param billId 单据ID
     */
    public final void setBillDetailsAllFinished(long billId) {
        log.info("标记明细已全部完成，单据ID:{}", billId);
        IDictionary status = getBillDetailsFinishStatus();
        FORBIDDEN.whenNull(status, "没有找到单据的所有明细完成状态");
        E bill = get(billId);
        bill.setStatus(status.getKey());
        update(bill);
        afterAllBillDetailFinished(bill.getId());
        if (status.equals(getFinishedStatus())) {
            log.info("明细完成状态是终态");
            setBillFinished(bill.getId());
        }
    }

    /**
     * 设置单据已完成
     *
     * @param billId 单据ID
     */
    public final void setBillFinished(long billId) {
        log.info("标记单据已完成，单据ID:{}, 单据类型:{}", billId, this.getClass().getSimpleName());
        IDictionary status = getFinishedStatus();
        FORBIDDEN.whenNull(status, "标记完成失败，没有找到完成状态");
        E bill = get(billId);
        beforeBillFinish(bill.copy());
        bill.setStatus(status.getKey());
        update(bill);
        afterBillFinished(bill.getId());
    }

    /**
     * 单据完成前置方法
     *
     * @param bill 单据
     */
    protected void beforeBillFinish(@NotNull E bill) {
        log.info("单据完成前置方法，单据ID:{}, 单据类型:{}", bill.getId(), this.getClass().getSimpleName());
    }

    /**
     * 添加明细完成数量
     *
     * @param sourceDetail 提交明细
     */
    public final void addDetailFinishQuantity(@NotNull D sourceDetail) {
        log.info("添加明细完成数量: 单据ID:{}, 明细数量:{}, 完成数量:{}, 单据类型:{}", sourceDetail.getBillId(), sourceDetail.getId(), sourceDetail.getQuantity(), this.getClass().getSimpleName());
        transactionHelper.run(() -> {
            // 查保存的明细
            D savedDetail = detailService.get(sourceDetail.getId());
            FORBIDDEN.when(savedDetail.getIsFinished(), "该明细已标记完成，无法再添加明细完成数量");
            // 更新保存明细完成数量 = 保存明细完成数量 + 提交完成数量
            double finishQuantity = NumberUtil.add(savedDetail.getFinishQuantity(), sourceDetail.getQuantity());
            // 如果完成数量 >= 数量 则标记明细完成
            savedDetail.setFinishQuantity(finishQuantity).setIsFinished(finishQuantity >= savedDetail.getQuantity());
            detailService.update(savedDetail);
            log.info("修改明细完成 数量:{} 是否完成:{}", finishQuantity, savedDetail.getIsFinished());

            // 明细添加成功后置方法
            afterDetailFinishAdded(savedDetail.getId(), sourceDetail);

            // 开始判断是否整个单据数量已超标
            List<D> details = detailService.getAllByBillId(savedDetail.getBillId());
            // 判断所有明细是否完成
            boolean isAllFinished = details.stream()
                    .allMatch(BaseBillDetailEntity::getIsFinished);
            log.info("所有明细是否已完成: {}", isAllFinished);
            if (!isAllFinished) {
                return;
            }
            setBillDetailsAllFinished(savedDetail.getBillId());
        });
    }

    /**
     * 添加完成数量成功后置
     *
     * @param detailId     明细ID
     * @param sourceDetail 提交明细
     */
    protected void afterDetailFinishAdded(long detailId, D sourceDetail) {
        log.info("添加完成数量成功后置，明细ID:{}, 提交明细:{}", detailId, sourceDetail);
    }

    /**
     * 单据完成的后置方法
     *
     * @param billId 单据ID
     * @apiNote 一般用于在当前单据完成后同步标记关联的其他单据为完成状态
     * @see #afterAllBillDetailFinished(long)
     */
    protected void afterBillFinished(Long billId) {
        log.info("单据完成的后置，单据ID:{}", billId);
    }

    /**
     * 单据所有明细完成的后置方法
     *
     * @param billId 单据ID
     * @apiNote 一般用于当前单据的所有明细都已完成，可能会创建其他的单据，也可能去修改其他单据的明细
     * @see #afterBillFinished(Long)
     */
    protected void afterAllBillDetailFinished(long billId) {
        log.info("单据所有明细完成的后置，单据ID:{}", billId);
    }

    /**
     * 单据明细保存后置方法
     *
     * @param bill 单据
     */
    protected void afterDetailSaved(@NotNull E bill) {
        log.info("单据明细保存后置，单据ID:{}", bill.getId());
    }

    @Override
    protected final E afterAppGet(@NotNull E bill) {
        List<D> details = detailService.getAllByBillId(bill.getId());
        bill.setDetails(details);
        return afterBillGet(bill);
    }

    /**
     * 单据获取后置
     *
     * @param bill 单据
     * @return 单据
     */
    protected E afterBillGet(@NotNull E bill) {
        log.info("单据获取后置，单据ID:{}", bill.getId());
        return bill;
    }

    @Override
    protected final void afterAppAdd(@NotNull E bill, @NotNull E source) {
        saveDetails(bill.getId(), source.getDetails());
        ConfigFlag configFlag = getAutoAuditConfigFlag();
        if (Objects.nonNull(configFlag)) {
            ConfigEntity config = Services.getConfigService().get(configFlag);
            if (config.booleanConfig()) {
                audit(bill.getId());
            }
        }
        TaskUtil.run(() -> afterBillAdd(bill.getId()));
    }

    /**
     * 单据添加后置
     *
     * @param id 单据ID
     */
    protected void afterBillAdd(long id) {
        log.info("单据添加后置，单据ID:{}", id);
    }

    @Override
    protected final void afterAppUpdate(@NotNull E bill, @NotNull E source) {
        saveDetails(bill.getId(), source.getDetails());
        afterBillUpdate(bill, source);
    }

    /**
     * 单据更新后置
     *
     * @param bill   单据
     * @param source 源数据
     */
    protected void afterBillUpdate(@NotNull E bill, @NotNull E source) {
        log.info("单据更新后置，单据ID:{}", bill.getId());
    }

    /**
     * 保存单据明细
     * <li>
     * 请不要再重写后直接调用 {@link #update(CurdEntity)} #{@link #updateWithNull(CurdEntity)}，避免出现调用循环。
     * </li>
     * <li>
     * 如需再次保存，请调用 {@link #updateToDatabase(CurdEntity)} }
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
     * 单据审核
     *
     * @param billId 单据ID
     */
    protected final void audit(long billId) {
        E bill = get(billId);
        FORBIDDEN.when(!canAudit(bill), "该单据状态无法审核");
        setAudited(bill);
        update(bill);
        TaskUtil.run(() -> afterBillAudited(bill.getId()));
    }

    /**
     * 单据审核的后置方法
     *
     * @param billId 单据ID
     * @apiNote 可以添加一些审核后的业务逻辑
     */
    protected void afterBillAudited(long billId) {
        log.info("单据审核后置方法: 单据ID:{}, 单据类型:{}", billId, this.getClass().getSimpleName());
    }

    /**
     * 设置为已审核状态
     *
     * @param bill 单据
     */
    public final void setAudited(@NotNull E bill) {
        bill.setStatus(getAuditedStatus().getKey());
    }

    /**
     * 设置为审核中状态
     *
     * @param bill 单据
     */
    public final void setAuditing(@NotNull E bill) {
        bill.setStatus(getAuditingStatus().getKey());
    }

    /**
     * 单据是否可审核
     *
     * @param bill 单据
     * @return 是否审核
     */
    public final boolean canAudit(@NotNull E bill) {
        return getAuditingStatus().equalsKey(bill.getStatus());
    }

    /**
     * 单据是否可驳回
     *
     * @param bill 单据
     * @return 是否可驳回
     */
    public final boolean canReject(@NotNull E bill) {
        return getAuditingStatus().equalsKey(bill.getStatus());
    }

    /**
     * 设置单据为驳回状态
     *
     * @param bill 单据
     */
    public final void setReject(@NotNull E bill) {
        bill.setStatus(getRejectedStatus().getKey());
    }

    /**
     * 单据是否可编辑
     *
     * @param bill 单据
     * @return 是否可编辑
     */
    public final boolean canEdit(@NotNull E bill) {
        return getRejectedStatus().equalsKey(bill.getStatus());
    }

    /**
     * 获取审核中状态
     *
     * @return 审核中状态
     */
    protected abstract IDictionary getAuditingStatus();

    /**
     * 获取已审核状态
     *
     * @return 已审核状态
     */
    protected abstract IDictionary getAuditedStatus();

    /**
     * 获取驳回状态
     *
     * @return 驳回状态
     */
    protected abstract IDictionary getRejectedStatus();

    /**
     * 获取所有明细均已完成的单据状态
     *
     * @return 所有明细均已完成的单据状态
     * @apiNote 可单独配置 {@link #getFinishedStatus()}
     */
    public abstract IDictionary getBillDetailsFinishStatus();

    /**
     * 获取单据已完成状态
     *
     * @return 单据已完成状态
     * @apiNote 默认为 {@link #getBillDetailsFinishStatus()}
     */
    public IDictionary getFinishedStatus() {
        log.info("获取单据已完成状态: {}", getBillDetailsFinishStatus().getLabel());
        return getBillDetailsFinishStatus();
    }
}
