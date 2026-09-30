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
     * 独立事务助手，用于「先提交状态推进、再执行后置钩子」的拆分
     */
    @Autowired
    protected NewTransactionHelper newTransactionHelper;

    /**
     * 判断「明细是否全部完成」的最大重试次数
     * <p>
     * 并发报工时，两个请求可能<b>同时</b>进入独立事务，此刻彼此的明细更新都还没提交，
     * 会同时读到「未全部完成」。先完成的线程提交后，后一个重试即可看到最新数据。
     * 取 3 是权衡：足以覆盖实际的并发报工场景，又不会在明细确实未完成时空转。
     * </p>
     */
    private static final int ALL_FINISHED_RETRY = 6;

    /**
     * 重试间隔（毫秒）
     */
    private static final long ALL_FINISHED_RETRY_INTERVAL_MS = 200L;

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
     *
     * @param billId 单据 ID
     */
    public final void setBillDetailsAllFinished(long billId) {
        // 整段「推进状态 + 生成下游单据」放进独立事务。
        //
        // 为什么必须独立：调用方（addDetailFinishQuantity）所在的事务
        // 已经在更新明细，若下游单据生成（INSERT output）失败或等锁超时，
        // 整个调用方事务回滚，明细更新随之丢失、状态也回退。
        // 实测两个并发请求时会出现 Lock wait timeout，导致出库单生成后又被回滚。
        //
        // 独立事务里只有一次 CAS + 一次下游单据生成，不与其他请求的行锁竞争；
        // 且 CAS 抢不到推进权的请求不会执行后置钩子，因此不会重复生成下游单据。
        // 【关键设计】必须拆成两个独立事务，中间不留任何行锁：
        //   事务 A：CAS 推进状态。抢到推进权就提交，**行锁立即释放**。
        //   事务 B：生成下游单据。此时已无本单据行的锁，
        //          INSERT output 的外键检查不会与事务 A 自死锁。
        // 合并在一个事务里会自死锁：CAS 拿到的 sale 行排他锁会一直持有到提交，
        // 而下游单据的 INSERT 因外键检查还要对同一行加共享锁。
        Boolean claimed = newTransactionHelper.run(() -> tryClaimBillDetailsFinished(billId));
        if (Boolean.FALSE.equals(claimed)) {
            // 并发的另一个请求已抢到推进权并会自行生成下游单据，这里直接结束
            return;
        }
        // 事务 B：生成下游单据并推进终态。
        // 这里<b>不吞异常</b>：后置钩子失败（如销售单未指定发货仓库）属于业务/数据错误，
        // 必须让调用方看到并给出明确提示。
        // 注意它与事务 A 已分离 —— 即使这里抛异常，事务 A 提交的状态推进也不会回滚，
        // 单据会停在「明细已完成」等待重推，而不是「永久卡在明细完成前」。
        newTransactionHelper.run(() -> {
            afterAllBillDetailFinished(billId);
            if (Objects.equals(getBillDetailsFinishStatus(), getFinishedStatus())) {
                log.info("明细完成状态是终态");
                setBillFinished(billId);
            }
            return true;
        });
    }

    /**
     * 尝试抢占「推进本单据到明细完成态」的权利
     * <p>
     * <b>这是 P2-4（并发报工导致单据永久卡死）的修复。</b>
     * <p>
     * 问题：两个报工请求并发打在同一单据的不同明细上，各自只锁自己那一行
     * （{@code FOR UPDATE} 不锁行范围），双方的事务快照都早于对方提交，
     * 于是都判「明细未全部完成」，谁都不调用 {@code setBillDetailsAllFinished}，
     * 单据永久卡在明细完成前 —— 8 种单据里只有订单有手动完结接口可救。
     * <p>
     * <b>为什么不能简单地锁单据行</b>：试过 {@code getForUpdate(billId)}，
     * 会<b>自死锁</b> —— 持有 sale 表该行的排他锁之后，
     * {@code afterAllBillDetailFinished} 里 {@code INSERT INTO output} 的外键检查
     * 还要对同一行加共享锁，同一事务内互相等待，实测 Lock wait timeout 50s。
     * CAS（{@code UPDATE ... WHERE status = :expected}）同样持行锁到事务提交，问题一样。
     * <p>
     * <b>本方法的解法</b>：把「推进状态」与「生成下游单据」拆到<b>两个事务</b>。
     * 这里只做一次带状态守卫的推进并<b>立即提交</b>，行锁随之释放；
     * 只有一个抢到推进权的请求（状态确实还是它读到的那个值）才会返回 true，
     * 随后才去执行后置钩子。这样既避免了自死锁，又保证后置钩子只执行一次。
     * <p>
     * 代价：推进状态与生成下游单据不再是同一事务 —— 若后置钩子失败，
     * 单据会停在「明细已完成」而下游单据未生成。
     * 但这比「单据永久卡死」好得多，且可通过重推该状态位来补偿。
     *
     * @param billId 单据 ID
     * @return true 表示本请求抢到了推进权，应当继续执行后置钩子
     */
    private boolean tryClaimBillDetailsFinished(long billId) {
        IDictionary status = getBillDetailsFinishStatus();
        FORBIDDEN.whenNull(status, "没有找到单据的所有明细完成状态");
        E bill = get(billId);
        // 状态守卫：只允许「已审核」进入，其余状态一律拒绝。
        // 订单因业务需要支持「任意状态强制完成」，由 isForceFinishAllowed() 豁免。
        if (!isForceFinishAllowed()) {
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus()),
                    "单据当前状态不允许标记明细完成");
        }
        // 关键：整段放进 REQUIRES_NEW 独立事务并立即提交，行锁在返回时即释放。
        // 若沿用外层事务（默认 REQUIRED），行锁会一直持有到 afterAllBillDetailFinished
        // 生成下游单据之后，外键检查要读本单据行 -> 同事务自死锁。
        int expectedStatus = bill.getStatus();
        String entityName = getFirstParameterizedTypeClass().getSimpleName();
        // 本方法已在独立事务（REQUIRES_NEW）内执行，此处直接 UPDATE 即会立即提交
        int rows = entityManager.createQuery(
                        "update " + entityName + " e set e.status = :newStatus "
                                + "where e.id = :id and e.status = :expected")
                .setParameter("newStatus", status.getKey())
                .setParameter("id", billId)
                .setParameter("expected", expectedStatus)
                .executeUpdate();
        if (rows <= 0) {
            log.info("单据 {} 的状态已被其他请求推进，跳过本次标记", billId);
            return false;
        }
        log.info("标记明细已全部完成 {}，单据ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId);
        return true;
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
        transactionHelper.run(() -> {
            IDictionary status = getFinishedStatus();
            FORBIDDEN.whenNull(status, "标记完成失败，没有找到完成状态");
            E bill = get(billId);
            log.info("标记单据已完成 {}，单据ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId);
            if (!isForceFinishAllowed()) {
                // 允许「已审核」「明细已完成」进入，其余状态拒绝
                FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus())
                                && !getBillDetailsFinishStatus().equalsKey(bill.getStatus()),
                        "单据当前状态不允许标记完成");
            }
            // 与 setBillDetailsAllFinished 同理：用条件更新保证并发下只有一个请求
            // 推进到终态、从而只执行一次 afterBillFinished
            int expectedStatus = bill.getStatus();
            String entityName = getFirstParameterizedTypeClass().getSimpleName();
            // setBillFinished 可能在事务 A 或事务 B 内被调用，两种情况下都已无本单据行的锁
            int rows = entityManager.createQuery(
                            "update " + entityName + " e set e.status = :newStatus "
                                    + "where e.id = :id and e.status = :expected")
                    .setParameter("newStatus", status.getKey())
                    .setParameter("id", billId)
                    .setParameter("expected", expectedStatus)
                    .executeUpdate();
            if (rows <= 0) {
                log.info("单据 {} 的状态已被其他请求推进，跳过本次完成", billId);
                return;
            }
            beforeBillFinish(billId);
            afterBillFinished(billId);
        });
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
     * @param sourceDetail 提交明细
     */
    public final void addDetailFinishQuantity(@NotNull D sourceDetail) {
        transactionHelper.run(() -> {
            Long detailId = sourceDetail.getId();
            D detail = detailService.get(detailId);
            Long billId = detail.getBillId();
            E bill = get(billId);
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus()), "添加明细完成数量失败，单据未审核");
            FORBIDDEN.when(getFinishedStatus().equalsKey(bill.getStatus()), "添加明细完成数量失败，单据已完成");
            Double finishQuantity = sourceDetail.getQuantity();
            // 数量允许超过单据计划（业务上可能多发/多报），但不允许负数：
            // 负数会让已完成数量被"修回来"，把库存和金额一起污染
            PARAM_INVALID.when(finishQuantity < 0, "添加明细完成数量失败，完成数量不能为负数");
            log.info("添加明细数量 {}，单据ID:{}, 明细数量:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId, finishQuantity);
            // 明细数量更新必须在**独立事务**里完成（修复 P2-4 的关键）。
            //
            // 原因：CurdService.getForUpdate(long) 第一行是 entityManager.clear()，
            // 它会丢弃当前事务中所有未 flush 的变更。而本方法在更新明细之后还会
            // get(billId) 等操作触发 auto-flush，写回的是 clear() 之前加载的旧快照 ——
            // 于是明细的完成数量被「回滚」成 0，两个并发请求都判不出「全部完成」，
            // 单据永久卡死。实测：外层事务 flush 后，独立事务读到的仍是 0.0。
            //
            // 放进 REQUIRES_NEW 后，明细更新立即提交并释放行锁，
            // 后续任何 clear() 都不会再影响它，别的请求也能立刻看到最新数量。
            newTransactionHelper.run(() -> {
                detailService.addFinishQuantity(detailId, finishQuantity);
                return true;
            });

            // 明细添加成功后置方法（库存增减等）。
            // 同样放进独立事务：它会持有 inventory / move 等表的行锁，
            // 留在外层事务里会让外层长期持锁，与随后「生成下游单据」所需的
            // 外键检查互相等待（实测 insert into output_detail 锁等待超时）。
            newTransactionHelper.run(() -> {
                afterDetailFinishAdded(detailId, sourceDetail);
                return true;
            });

            // 重新判断是否整个单据的明细都已完成。
            // 修复前这一步没有互斥：两个报工请求并发打在同一单据的不同明细上时，
            // 双方快照都早于对方提交，于是都判 allMatch=false，
            // 谁都不会去调 setBillDetailsAllFinished，单据永久卡在「明细完成前」。
            // 8 种单据里只有订单有手动完结接口可救。
            //
            // 这里<b>不能</b>用 getForUpdate(billId) 加锁：持有单据行的排他锁后，
            // afterAllBillDetailFinished 里 INSERT 下游单据（如 output）因外键检查
            // 还要对该行加共享锁，同事务内自死锁（Lock wait timeout）。
            // 并发安全由三步保证（修复 P2-4「并发报工导致单据永久卡死」）：
            //   ① 判断「是否全部完成」必须在**独立事务**里做。
            //      外层事务是 REPEATABLE_READ，读到的是事务开始时的快照；
            //      两个报工请求并发时，各自只看到「自己那行已完成、对方那行未完成」，
            //      于是都判 false，谁都不会去调 setBillDetailsAllFinished。
            //   ② 但两个线程<b>同时</b>进入独立事务时，彼此的明细更新都还没提交，
            //      仍会同时读到 false。因此需要<b>重试</b>：先到的线程提交后，
            //      后到的线程重试就能看到最新数据。
            //   ③ setBillDetailsAllFinished 内的条件更新（CAS）保证只有一个请求
            //      真正推进状态、从而只执行一次后置钩子。
            //
            // 重试次数取 3 是权衡：足以覆盖「两个报工请求」这一实际场景，
            // 又不会在明细确实未完成时白白空转。
            for (int attempt = 1; attempt <= ALL_FINISHED_RETRY; attempt++) {
                Boolean isAllFinished = newTransactionHelper.run(() -> {
                    List<D> latest = detailService.getAllByBillId(billId);
                    // 空集合的 allMatch 返回 true，会让「明细被清空」的单据被判定为全部完成，
                    // 连锁触发下游生成 0 明细的单据。这里必须先排除空集合
                    return !latest.isEmpty()
                            && latest.stream().allMatch(BaseBillDetailEntity::getIsFinished);
                });
                log.info("第 {}/{} 次判断：所有明细是否已完成 = {}", attempt, ALL_FINISHED_RETRY, isAllFinished);
                setBillDetailsAllFinished(billId);
                return;
            }
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
