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
 * <h1>单据 Service 基类</h1>
 *
 * @param <E>   单据实体
 * @param <R>   单据数据源
 * @param <D>   明细实体
 * @param <DS>  明细 Service
 * @param <DR>> 明细数据源
 * @author Hamm.cn
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
     * 获取自动审核配置
     *
     * @return 配置标识
     */
    protected ConfigFlag getAutoAuditConfigFlag() {
        log.info("获取自动审核配置, 无需自动审核 {}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()));
        return null;
    }

    /**
     * 设置单据所有明细都已完成
     * <p>
     * 整体在<b>同一个事务</b>内完成：加锁单据行 → 推进状态 → 生成下游单据 → 推进终态。
     * 不拆事务，中途失败会整体回滚，不会出现「状态已推进但下游单据没生成」的中间态。
     * </p>
     *
     * @param billId 单据 ID
     */
    public final void setBillDetailsAllFinished(long billId) {
        transactionHelper.run(() -> applyBillDetailsFinished(getForUpdate(billId)));
    }

    /**
     * 推进单据到「明细已完成」并执行后置钩子（使用已加锁的单据实例）
     * <p>
     * <b>这是 P2-4「并发报工导致单据永久卡死」的修复。</b>
     * </p>
     * <p>
     * 问题：两个报工请求并发打在同一单据的不同明细上，各自只锁自己那一行明细，
     * 双方的事务快照都早于对方提交，于是都判「明细未全部完成」，
     * 谁都不推进单据，单据永久卡在明细完成前 ——
     * 8 种单据里只有订单有手动完结接口可救。
     * </p>
     * <p>
     * <b>解法是加锁，把并发请求串行化。</b>调用方
     * {@link #addDetailFinishQuantity} 已锁住单据行，这里直接复用已加锁的实例；
     * 对外的 {@code setBillDetailsAllFinished(long)} 会自行加锁单据行。
     * 加锁顺序对所有入口一致，不会交叉等待。
     * </p>
     * <p>
     * 锁住单据行之后，生成下游单据（{@code INSERT INTO output}）的外键检查
     * 虽然也要读这一行，但<b>同一事务内重复加锁 InnoSQL 直接放行</b>，不会自死锁。
     * 真正会超时的场景是<b>两个不同事务</b>争抢同一行 —— 串行化之后就不存在了。
     * </p>
     *
     * @param bill 已加锁的单据
     */
    private void applyBillDetailsFinished(@NotNull E bill) {
        long billId = bill.getId();
        IDictionary status = getBillDetailsFinishStatus();
        FORBIDDEN.whenNull(status, "没有找到单据的所有明细完成状态");
        log.info("标记明细已全部完成 {}，单据ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId);
        // 状态守卫：修复前这里不做任何检查，任何能调到该方法的路径
        // 都能把单据从任意状态（例如「审核中」）直接推到终态。
        // 订单因业务需要支持「任意状态强制完成」，由 isForceFinishAllowed() 豁免。
        if (!isForceFinishAllowed()) {
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus()),
                    "单据当前状态不允许标记明细完成");
        }
        // 幂等：已是「明细已完成」说明别的请求（或本请求的另一次调用）已经推进过，
        // 再执行一次会重复生成下游单据。8 种单据的 audited 与 detailsFinish 状态
        // 均不相同（见各 XxxService），所以这个判断不会误伤首次调用。
        if (status.equalsKey(bill.getStatus())) {
            log.info("单据 {} 已是明细完成状态，跳过", billId);
            return;
        }
        updateToDatabase(getEntityInstance(billId).setStatus(status.getKey()));
        afterAllBillDetailFinished(billId);
        if (status.equals(getFinishedStatus())) {
            log.info("明细完成状态是终态");
            // 必须传已加锁的实例：再调 setBillFinished(billId) 会走一次 getForUpdate，
            // 而它第一行是 entityManager.clear()，会把上面刚推进的状态冲掉
            applyBillFinished(bill);
        }
    }

    /**
     * 是否允许绕过状态守卫强制完成单据
     * <p>
     * 默认不允许。订单存在「允许在任何情况下手动完成订单」的业务需求
     * （见 {@code ConfigFlag} 说明），由 {@code OrderService} 重写为 {@code true}。
     * </p>
     *
     * @return true 表示跳过状态守卫
     */
    protected boolean isForceFinishAllowed() {
        return false;
    }

    /**
     * 设置单据已完成
     *
     * @param billId 单据 ID
     */
    public final void setBillFinished(long billId) {
        transactionHelper.run(() -> applyBillFinished(getForUpdate(billId)));
    }

    /**
     * 推进单据到终态并执行后置钩子（使用已加锁的单据实例）
     *
     * @param bill 已加锁的单据
     */
    private void applyBillFinished(@NotNull E bill) {
        long billId = bill.getId();
        {
            IDictionary status = getFinishedStatus();
            FORBIDDEN.whenNull(status, "标记完成失败，没有找到完成状态");
            log.info("标记单据已完成 {}，单据ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId);
            if (!isForceFinishAllowed()) {
                // 允许「已审核」「明细已完成」进入，其余状态拒绝
                FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus())
                                && !getBillDetailsFinishStatus().equalsKey(bill.getStatus()),
                        "单据当前状态不允许标记完成");
            }
            // 幂等：已是终态则不重复执行 afterBillFinished，
            // 否则会重复生成下游单据（如重复建入库单）
            if (status.equalsKey(bill.getStatus())) {
                log.info("单据 {} 已是完成状态，跳过", billId);
                return;
            }
            updateToDatabase(getEntityInstance(billId).setStatus(status.getKey()));
            beforeBillFinish(billId);
            afterBillFinished(billId);
        }
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
     * 加锁读取单据，供明细 Service 在改动明细前串行化
     * <p>
     * 明细归属单据，对同一单据的并发改动必须先拿到单据行锁。
     * 暴露成 {@code public} 是因为 {@code BaseBillDetailService} 拿不到本类的
     * {@code protected getForUpdate}。
     * </p>
     *
     * @param billId 单据 ID
     * @return 加锁后的单据
     */
    public final E getBillForUpdate(long billId) {
        return getForUpdate(billId);
    }

    /**
     * 添加明细完成数量
     * <p>
     * 取单据 ID 时优先用请求里带来的 {@code billId}，这样
     * {@link #addDetailFinishQuantity(long, D)} 里<b>锁单据行就是第一个数据库操作</b>。
     * </p>
     *
     * @param sourceDetail 提交明细（需带上 billId）
     */
    public final void addDetailFinishQuantity(@NotNull D sourceDetail) {
        Long billId = sourceDetail.getBillId();
        if (Objects.isNull(billId)) {
            // 兜底：前端没带 billId 时只能先查一次。
            // 注意这会让「一致性读」提前发生、读视图提前固定，
            // 并发下的判断可能读到旧快照 —— 所以正常路径务必由前端带上 billId
            log.warn("提交明细未携带单据ID，退化为查库获取，并发判断可能不准，detailId:{}", sourceDetail.getId());
            billId = detailService.get(sourceDetail.getId()).getBillId();
        }
        addDetailFinishQuantity(billId, sourceDetail);
    }

    /**
     * 添加明细完成数量
     * <p>
     * 全流程在<b>同一个事务</b>内完成：锁单据行 → 更新明细 → 库存增减 → 推进单据
     * → 生成下游单据。中途任何一步失败都整体回滚，不会出现
     * 「明细已更新但单据没推进」或「状态已推进但下游单据没生成」的中间态。
     * </p>
     * <p>
     * <b>并发安全（P2-4）只靠锁单据行这一处，明细不加锁。</b>
     * 对同一张单据的并发报工全部在单据行上排队，先到的请求整个事务提交后，
     * 后到的才能继续。加锁顺序「单据行 → 本行明细 → 库存行」对所有请求一致，
     * 不会交叉等待。
     * </p>
     * <p>
     * <b>锁单据行必须���事务里的第一个数据库操作</b>，这是整个修复成立的前提。
     * MySQL 在 {@code REPEATABLE_READ} 下，普通 SELECT 是快照读，
     * 读视图在<b>第一次一致性读</b>时固定，此后本事务再也看不到别人的提交。
     * 如果在加锁之前先做过任何普通读（哪怕是 {@code getForUpdate} 顺带加载
     * EAGER 关联产生的查询），读视图就会在<b>排队等待单据锁之前</b>被固定下来，
     * 等排到队时仍看不到前一个请求的提交，于是又会出现
     * 「双方都判明细未全部完成、单据永久卡死」。
     * 把加锁放在最前面，读视图就建立于锁等待结束之后，读到的自然是最新已提交数据。
     * </p>
     * <p>
     * 同事务内生成下游单据（{@code INSERT INTO output}）的外键检查也要读单据行，
     * 但<b>同一事务内重复加锁 InnoDB 直接放行</b>，不会自死锁。
     * </p>
     *
     * @param billId      单据 ID
     * @param sourceDetail 提交明细
     */
    public final void addDetailFinishQuantity(long billId, @NotNull D sourceDetail) {
        transactionHelper.run(() -> {
            Long detailId = sourceDetail.getId();
            // ① 锁单据行 —— 全流程第一个数据库操作，串行化点
            E bill = getForUpdate(billId);
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus()), "添加明细完成数量失败，单据未审核");
            FORBIDDEN.when(getFinishedStatus().equalsKey(bill.getStatus()), "添加明细完成数量失败，单据已完成");
            Double finishQuantity = sourceDetail.getQuantity();
            // 数量允许超过单据计划（业务上可能多发/多报），但不允许负数：
            // 负数会让已完成数量被"修回来"，把库存和金额一起污染
            PARAM_INVALID.when(finishQuantity < 0, "添加明细完成数量失败，完成数量不能为负数");
            log.info("添加明细数量 {}，单据ID:{}, 明细数量:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId, finishQuantity);

            detailService.addFinishQuantity(detailId, finishQuantity);
            // 明细添加成功后置方法（库存增减等）
            afterDetailFinishAdded(detailId, sourceDetail);

            // ② 此刻才第一次普通读明细：读视图建立于锁等待结束之后，
            //    读到的就是最新已提交数据
            if (!detailService.isAllDetailFinished(billId)) {
                return;
            }
            applyBillDetailsFinished(bill);
        });
    }

    /**
     * 添加完成数量成功后置
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
     * @apiNote 一般用于在当前单据完成后同步标记关联的其他单据为完成状态
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
     * @apiNote 一般用于当前单据的所有明细都已完成，可能会创建其他的单据，也可能去修改其他单据的明细
     * @see #afterBillFinished(long)
     */
    protected void afterAllBillDetailFinished(long billId) {
        log.info("单据所有明细已完成 {}，单据ID:{}",
                ReflectUtil.getDescription(getFirstParameterizedTypeClass()),
                billId
        );
    }

    /**
     * 单据明细保存后置方法
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
     * <p>
     * {@code Curd} 枚举里没有 {@code Publish}，所以 {@code @Extends(exclude = ...)}
     * 排除不掉它，只能在这里拦。
     * <p>
     * 原因：发布会把 {@code isPublished} 置 true，之后 {@code BaseController}
     * 会拒绝该数据的<b>一切修改与删除</b>，而单据的 {@code Delete} 接口本就
     * 被 {@code BaseBillController} 排除 —— 于是这张单据永久卡死，没有任何自愈手段。
     * </p>
     *
     * @param bill 单据
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
     * 单据获取后置
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
     * 单据添加后置
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
     * 单据更新后置
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
     * <li>
     * 请不要再重写后直接调用 {@link #update(CurdEntity)} ，避免出现调用循环。
     * </li>
     * <li>
     * 如需再次保存，请调用 {@link #updateToDatabase(CurdEntity)} }
     * </li>
     *
     * @param billId  单据 ID
     * @param details 明细列表
     */
    private void saveDetails(long billId, List<D> details) {
        detailService.saveDetails(billId, details);
        afterDetailSaved(billId);
    }

    /**
     * 单据审核
     *
     * @param billId 单据 ID
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
     * 单据驳回
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
     * 单据审核的后置方法
     *
     * @param billId 单据 ID
     * @apiNote 可以添加一些审核后的业务逻辑
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
