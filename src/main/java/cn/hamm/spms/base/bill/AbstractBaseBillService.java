package cn.hamm.spms.base.bill;

import cn.hamm.airpower.core.ReflectUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.airpower.curd.base.CurdEntity;
import cn.hamm.airpower.curd.helper.TransactionHelper;
import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailService;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>单据服务基类</h1>
 *
 * @param <E>  单据实体
 * @param <R>  单据数据源
 * @param <D>  明细实体
 * @param <DS> 明细 Service
 * @param <DR> 明细数据源
 * @author Hamm.cn
 * @apiNote 所有状态推进都在同一事务内完成，并且以锁单据行（{@code SELECT ... FOR UPDATE}）作为
 * 第一个数据库操作。InnoDB 的读视图在第一条查询时就已固定，锁排在查询之后等于没排
 */
@Slf4j
public abstract class AbstractBaseBillService<
        E extends AbstractBaseBillEntity<E, D>,
        R extends BaseRepository<E>,
        D extends BaseBillDetailEntity<D>,
        DS extends BaseBillDetailService<D, DR>,
        DR extends BaseBillDetailRepository<D>
        > extends BaseService<E, R> {

    @Autowired(required = false)
    protected DS detailService;

    @Autowired
    protected TransactionHelper transactionHelper;

    /**
     * 获取自动审核配置项
     *
     * @return 配置项，返回 {@code null} 表示该单据不自动审核
     */
    protected ConfigFlag getAutoAuditConfigFlag() {
        log.info("获取自动审核配置, 无需自动审核 {}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()));
        return null;
    }

    /**
     * 设置单据所有明细都已完成
     *
     * @param billId 单据 ID
     * @apiNote 全流程在同一事务内完成，加锁单据行必须是第一个数据库操作，
     * 否则读视图在排队前就固定了，并发下仍会重复生成下游单据
     */
    public final void setBillDetailsAllFinished(long billId) {
        transactionHelper.run(() -> applyBillDetailsFinished(getForUpdate(billId)));
    }

    /**
     * 推进单据到「明细已完成」并执行后置钩子
     *
     * @param bill 已加锁的单据
     * @apiNote 必须传入已加锁的单据实例，否则并发下会重复执行
     * {@code afterAllBillDetailFinished}、生成多张下游单据
     */
    private void applyBillDetailsFinished(@NotNull E bill) {
        long billId = bill.getId();
        IDictionary status = getBillDetailsFinishStatus();
        FORBIDDEN.whenNull(status, "没有找到单据的所有明细完成状态");
        log.info("标记明细已全部完成 {}，单据ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId);
        // 只允许「已审核」进入；订单的「任意状态强制完成」由 isForceFinishAllowed() 豁免
        if (!isForceFinishAllowed()) {
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus()),
                    "单据当前状态不允许标记明细完成");
        }
        // 幂等：已是目标状态说明已被推进过，再执行会重复生成下游单据
        if (status.equalsKey(bill.getStatus())) {
            log.info("单据 {} 已是明细完成状态，跳过", billId);
            return;
        }
        updateToDatabase(getEntityInstance(billId).setStatus(status.getKey()));
        afterAllBillDetailFinished(billId);
        if (status.equals(getFinishedStatus())) {
            log.info("明细完成状态是终态");
            applyBillFinished(bill);
        }
    }

    /**
     * 是否允许绕过状态守卫强制完成单据
     *
     * @return true 表示跳过状态守卫
     * @apiNote 默认不允许；订单因「允许任意状态手动完成」的业务需求重写为 true
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isForceFinishAllowed() {
        return false;
    }

    /**
     * 设置单据已完成
     *
     * @param billId 单据 ID
     * @apiNote 同样要求锁单据行是第一个数据库操作，否则并发下会重复执行 {@code afterBillFinished}
     */
    public final void setBillFinished(long billId) {
        transactionHelper.run(() -> applyBillFinished(getForUpdate(billId)));
    }

    /**
     * 推进单据到终态并执行后置钩子
     *
     * @param bill 已加锁的单据
     * @apiNote 必须传入已加锁的单据实例，否则并发下会重复执行 {@code afterBillFinished}
     */
    private void applyBillFinished(@NotNull E bill) {
        long billId = bill.getId();
        IDictionary status = getFinishedStatus();
        FORBIDDEN.whenNull(status, "标记完成失败，没有找到完成状态");
        log.info("标记单据已完成 {}，单据ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId);
        // 允许「已审核」「明细已完成」进入，其余状态拒绝
        if (!isForceFinishAllowed()) {
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus())
                            && !getBillDetailsFinishStatus().equalsKey(bill.getStatus()),
                    "单据当前状态不允许标记完成");
        }
        // 幂等：已是终态则不重复执行 afterBillFinished，否则会重复生成下游单据
        if (status.equalsKey(bill.getStatus())) {
            log.info("单据 {} 已是完成状态，跳过", billId);
            return;
        }
        updateToDatabase(getEntityInstance(billId).setStatus(status.getKey()));
        beforeBillFinish(billId);
        afterBillFinished(billId);
    }

    /**
     * 单据完成前置方法
     *
     * @param billId 单据 ID
     */
    protected void beforeBillFinish(long billId) {
        log.info("标记单据完成前 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
    }

    /**
     * 添加明细完成数量
     *
     * @param sourceDetail 提交明细，需带上 billId
     * @apiNote 正常路径要求前端带上 billId，好让加锁排在所有查询之前
     */
    public final void addDetailFinishQuantity(@NotNull D sourceDetail) {
        Long billId = sourceDetail.getBillId();
        if (Objects.isNull(billId)) {
            // 兜底：没带 billId 就得先查一次，这会提前固定读视图，
            // 并发下的判断可能读到旧快照，所以正常路径必须由前端带上 billId
            log.warn("提交明细未携带单据ID，退化为查库获取，并发判断可能不准，detailId:{}", sourceDetail.getId());
            billId = detailService.get(sourceDetail.getId()).getBillId();
        }
        addDetailFinishQuantity(billId, sourceDetail);
    }

    /**
     * 添加明细完成数量
     *
     * @param billId       单据 ID
     * @param sourceDetail 提交明细
     * @apiNote 并发报工只在单据行上排队，明细与库存不加锁。锁单据行必须是第一个数据库操作，
     * 否则读视图在排队前就固定了，排到队时看不到前一个请求的提交，双方都判「明细未全部完成」而卡死
     */
    public final void addDetailFinishQuantity(long billId, @NotNull D sourceDetail) {
        transactionHelper.run(() -> {
            Long detailId = sourceDetail.getId();
            // 锁单据行：全流程第一个数据库操作，并发报工在这里排队
            E bill = getForUpdate(billId);
            // 「已完成」要先判：已完成的单据同样不满足「已审核」，
            // 顺序反了会让它报「单据未审核」，与实际状态不符
            FORBIDDEN.when(getFinishedStatus().equalsKey(bill.getStatus()), "添加明细完成数量失败，单据已完成");
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus()), "添加明细完成数量失败，单据未审核");
            Double finishQuantity = sourceDetail.getQuantity();
            // 数量允许超过单据计划（业务上可能多发/多报），但不允许负数：
            // 负数会让已完成数量被"修回来"，把库存和金额一起污染
            PARAM_INVALID.when(finishQuantity < 0, "添加明细完成数量失败，完成数量不能为负数");
            log.info("添加明细数量 {}，单据ID:{}, 明细数量:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId, finishQuantity);

            detailService.addFinishQuantity(detailId, finishQuantity);
            // 明细添加成功后置方法（库存增减等）
            afterDetailFinishAdded(detailId, sourceDetail);

            // 此刻才第一次普通读明细，读视图建立于锁等待结束之后，读到的是最新已提交数据
            if (!detailService.isAllDetailFinished(billId)) {
                return;
            }
            applyBillDetailsFinished(bill);
        });
    }

    /**
     * 添加完成数量后的后置方法
     *
     * @param detailId     明细 ID
     * @param sourceDetail 提交明细
     */
    protected void afterDetailFinishAdded(long detailId, @NotNull D sourceDetail) {
        log.info("添加完成数量成功后 {}，明细ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), detailId);
    }

    /**
     * 单据完成的后置方法
     *
     * @param billId 单据 ID
     * @apiNote 一般用于在当前单据完成后，同步把关联的其他单据也标记为完成
     * @see #afterAllBillDetailFinished(long)
     */
    protected void afterBillFinished(long billId) {
        log.info("单据已完成 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
    }

    /**
     * 单据所有明细完成的后置方法
     *
     * @param billId 单据 ID
     * @apiNote 一般用于在当前单据的所有明细都已完成后创建其他单据，
     * 或去修改其他单据的明细
     * @see #afterBillFinished(long)
     */
    protected void afterAllBillDetailFinished(long billId) {
        log.info("单据所有明细已完成 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
    }

    /**
     * 单据明细保存后的后置方法
     *
     * @param billId 单据 ID
     */
    protected void afterDetailSaved(long billId) {
        log.info("单据明细保存成功 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
    }

    /**
     * 单据不支持发布
     *
     * @param bill 单据
     * @apiNote 发布后 {@code BaseController} 会拒绝该数据的一切修改与删除，而单据的 Delete 接口
     * 本就被排除，这张单据就永久卡死了。{@code Curd} 没有 Publish 枚举项，
     * {@code @Extends(exclude)} 排除不掉，只能在这里拦
     */
    @Override
    protected void beforePublish(@NotNull E bill) {
        throw new UnsupportedOperationException("单据不支持发布操作");
    }

    @Override
    protected final E afterAppGet(@NotNull E bill) {
        List<D> details = detailService.getAllByBillId(bill.getId());
        bill.setDetails(details);
        return afterBillGet(bill);
    }

    /**
     * 单据查询后的后置方法
     *
     * @param bill 单据
     * @return 单据
     */
    protected E afterBillGet(@NotNull E bill) {
        log.info("查询单据详情后 {}，单据ID:{} {}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                bill.getId(),
                bill.getBillCode()
        );
        return bill;
    }

    @Override
    protected void afterAppAdd(long billId, @NotNull E source) {
        E bill = get(billId);
        saveDetails(billId, source.getDetails());
        ConfigFlag configFlag = getAutoAuditConfigFlag();
        if (Objects.nonNull(configFlag)) {
            ConfigEntity config = SystemServices.getConfigService().get(configFlag);
            if (config.booleanConfig()) {
                audit(bill.getId());
            }
        }
        afterBillAdd(bill.getId());
    }

    /**
     * 单据添加后的后置方法
     *
     * @param billId 单据 ID
     */
    protected void afterBillAdd(long billId) {
        log.info("单据添加后 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
    }

    @Override
    protected void afterAppUpdate(long billId, @NotNull E source) {
        saveDetails(billId, source.getDetails());
        afterBillUpdate(billId, source);
    }

    /**
     * 单据更新后的后置方法
     *
     * @param billId 单据 ID
     * @param source 源数据
     */
    protected void afterBillUpdate(long billId, @NotNull E source) {
        log.info("单据修改后 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
    }

    /**
     * 保存单据明细
     *
     * @param billId  单据 ID
     * @param details 明细列表
     * @apiNote 重复保存时必须调用 {@link #updateToDatabase(CurdEntity)} 而不是
     * {@link #update(CurdEntity)}，后者会回到本方法形成调用循环
     */
    private void saveDetails(long billId, List<D> details) {
        detailService.saveDetails(billId, details);
        afterDetailSaved(billId);
    }

    /**
     * 审核单据
     *
     * @param billId 单据 ID
     * @apiNote 方法为 {@code final}，子类只能通过 {@link #afterBillAudited(long)} 插入审核后逻辑
     */
    protected final void audit(long billId) {
        transactionHelper.run(() -> {
            E bill = get(billId);
            FORBIDDEN.when(!canAudit(bill), "该单据状态无法审核");
            bill = getEntityInstance(billId);
            setAudited(bill);
            updateToDatabase(bill);
            afterBillAudited(billId);
        });
    }

    /**
     * 驳回单据
     *
     * @param billId 单据 ID
     */
    protected final void reject(long billId) {
        transactionHelper.run(() -> {
            E bill = get(billId);
            FORBIDDEN.when(!canReject(bill), "该单据状态无法驳回");
            bill = getEntityInstance(billId);
            setReject(bill);
            bill.setRejectReason(bill.getRejectReason());
            updateToDatabase(bill);
        });
    }

    /**
     * 单据审核后的后置方法
     *
     * @param billId 单据 ID
     */
    protected void afterBillAudited(long billId) {
        log.info("单据审核后 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
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
     * @return 是否可审核
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
     * @apiNote 这是子类必须实现的状态；是否等于终态由 {@link #getFinishedStatus()} 决定，
     * 未重写时「明细已完成」就是终态
     */
    public abstract IDictionary getBillDetailsFinishStatus();

    /**
     * 获取单据已完成状态
     *
     * @return 单据已完成状态
     * @apiNote 默认为 {@link #getBillDetailsFinishStatus()}，即明细全部完成即单据完成
     */
    public IDictionary getFinishedStatus() {
        log.info("获取单据已完成状态: {}", getBillDetailsFinishStatus().getLabel());
        return getBillDetailsFinishStatus();
    }
}
