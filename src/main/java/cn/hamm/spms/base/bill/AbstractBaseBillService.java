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
     *
     * @param billId 单据 ID
     */
    public final void setBillDetailsAllFinished(long billId) {
        transactionHelper.run(() -> setBillDetailsAllFinished(get(billId)));
    }

    /**
     * 设置单据所有明细都已完成（使用已加载的单据实例）
     * <p>
     * 与 {@link #setBillDetailsAllFinished(long)} 的区别：不再重复 {@code get(billId)}。
     * {@code afterAppGet} 每次都会重查一遍全部明细，而调用链
     * {@code addDetailFinishQuantity -> setBillDetailsAllFinished -> setBillFinished}
     * 里有多处 {@code get}，实测一次报工会重复查 4~5 次相同的明细 SQL。
     * 传入已加锁的实例即可省掉这些重复查询。
     * </p>
     *
     * @param bill 已加载的单据
     */
    private void setBillDetailsAllFinished(@NotNull E bill) {
        long billId = bill.getId();
        IDictionary status = getBillDetailsFinishStatus();
        FORBIDDEN.whenNull(status, "没有找到单据的所有明细完成状态");
        log.info("标记明细已全部完成 {}，单据ID:{}", ReflectUtil.getDescription(getFirstParameterizedTypeClass()), billId);
        // 状态守卫：修复前这里不做任何检查，任何能调到该方法的路径
        // 都能把单据从任意状态（例如「审核中」）直接推到终态。
        // 只允许「已审核」进入，其余状态一律拒绝。
        // 订单因业务需要支持「任意状态强制完成」，由 isForceFinishAllowed() 豁免。
        if (!isForceFinishAllowed()) {
            FORBIDDEN.when(!getAuditedStatus().equalsKey(bill.getStatus()),
                    "单据当前状态不允许标记明细完成");
        }
        // ⚠️ 这里<b>不能</b>加「已是目标状态就 return」的幂等判断。
        // 销售单、入库单等单据的 getAuditedStatus() 与 getBillDetailsFinishStatus()
        // 是同一个状态（如均为 OUTPUTTING），首次调用时该条件就已成立，
        // 提前返回会导致「审核完的单据永远推进不到完成态」，
        // afterAllBillDetailFinished 不执行、下游单据（出库单/入库单）也不会生成。
        //
        // ⚠️ P2-4（并发报工导致单据永久卡死）在本轮<b>未能安全修复</b>，
        // 两种方案均实测失败，详见 12-P2修复报告-第三批.md 的「暂缓项」一节：
        //   ① getForUpdate(billId) 锁单据行 —— 持排他锁后，
        //      afterAllBillDetailFinished 里 INSERT output 的外键检查
        //      还要对该行加共享锁，同事务自死锁（Lock wait timeout 50s）。
        //   ② 单条 UPDATE 的条件更新（CAS）—— bulk update 同样持行锁
        //      直到事务提交，问题相同；实测 remove 后 10/10 通过、
        //      加回即 50s 超时。
        // 结论：在当前「推进状态与生成下游单据在同一调用链」的结构下，
        // 任何对单据行的写锁都会与下游单据的外键检查冲突。
        // 需要先解耦这两步（例如下游单据延后生成，或改为先插下游单据再推状态）。
        updateToDatabase(getEntityInstance(billId).setStatus(status.getKey()));
        afterAllBillDetailFinished(billId);
        if (status.equals(getFinishedStatus())) {
            log.info("明细完成状态是终态");
            setBillFinished(billId);
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
            updateToDatabase(getEntityInstance(billId).setStatus(status.getKey()));
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
            detailService.addFinishQuantity(detailId, finishQuantity);

            // 明细添加成功后置方法
            afterDetailFinishAdded(detailId, sourceDetail);

            // 重新判断是否整个单据的明细都已完成。
            // 修复前这一步没有互斥：两个报工请求并发打在同一单据的不同明细上时，
            // 双方快照都早于对方提交，于是都判 allMatch=false，
            // 谁都不会去调 setBillDetailsAllFinished，单据永久卡在「明细完成前」。
            // 8 种单据里只有订单有手动完结接口可救。
            //
            // 这里<b>不能</b>用 getForUpdate(billId) 加锁：持有单据行的排他锁后，
            // afterAllBillDetailFinished 里 INSERT 下游单据（如 output）因外键检查
            // 还要对该行加共享锁，同事务内自死锁（Lock wait timeout）。
            // 并发安全改由 setBillDetailsAllFinished 内的条件更新（CAS）保证。
            List<D> details = detailService.getAllByBillId(billId);
            // 空集合的 allMatch 返回 true，会让「明细被清空」的单据被判定为全部完成，
            // 连锁触发下游生成 0 明细的单据。这里必须先排除空集合
            boolean isAllFinished = !details.isEmpty()
                    && details.stream().allMatch(BaseBillDetailEntity::getIsFinished);
            log.info("所有明细是否已完成: {}", isAllFinished);
            if (!isAllFinished) {
                return;
            }
            setBillDetailsAllFinished(get(billId));
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
