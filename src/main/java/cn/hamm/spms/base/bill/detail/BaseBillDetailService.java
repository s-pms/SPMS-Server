package cn.hamm.spms.base.bill.detail;

import cn.hamm.airpower.core.NumberUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>单据明细服务基类</h1>
 *
 * @param <E> 明细实体
 * @param <R> 明细数据源
 * @author Hamm.cn
 */
@Slf4j
public class BaseBillDetailService<
        E extends BaseBillDetailEntity<E>,
        R extends BaseBillDetailRepository<E>
        > extends BaseService<E, R> {

    /**
     * 根据单据 ID 删除所有明细
     *
     * @param billId 单据 ID
     * @apiNote 逐条走 {@code delete(id)} 以触发钩子，不要换成
     * {@code repository.deleteByBillId} 这类派生批量删除，它既不触发 JPA 生命周期回调也不做级联
     */
    public final void deleteAllByBillId(Long billId) {
        transactionHelper.run(() -> {
            List<E> exists = getAllByBillId(billId);
            exists.forEach(detail -> delete(detail.getId()));
            log.info("已删除单据 {} 的 {} 条明细", billId, exists.size());
        });
    }

    /**
     * 查询指定单据的所有明细
     *
     * @param billId 单据 ID
     * @return 明细
     */
    public final List<E> getAllByBillId(Long billId) {
        return repository.getAllByBillId(billId);
    }


    /**
     * 判断单据的全部明细是否都已完成
     *
     * @param billId 单据 ID
     * @return true 表示全部明细都已完成
     * @apiNote 调用方必须已持有单据行锁：本方法要是提前到加锁之前读，就会固定住旧读视图，
     * 导致并发下误判「未全部完成」而卡死。空集合判否是因为
     * {@code allMatch} 在空集合上返回 {@code true}，会让明细被清空的单据连锁生成
     * 0 明细的下游单据
     */
    public final boolean isAllDetailFinished(Long billId) {
        List<E> details = getAllByBillId(billId);
        if (details.isEmpty()) {
            log.info("单据 {} 没有明细，不算全部完成", billId);
            return false;
        }
        boolean allFinished = details.stream().allMatch(BaseBillDetailEntity::getIsFinished);
        log.info("单据 {} 明细数 {}，全部完成 = {}", billId, details.size(), allFinished);
        return allFinished;
    }

    /**
     * 保存指定单据的明细
     *
     * @param billId  单据 ID
     * @param details 明细列表
     * @apiNote 先按 billId 全删再全插，明细的 {@code id} 会全部换新，
     * 外部系统不要长期持有明细 ID
     */
    public final void saveDetails(long billId, @NotNull List<E> details) {
        transactionHelper.run(() -> {
            deleteAllByBillId(billId);
            details.forEach(detail -> add(detail.setBillId(billId)));
        });
    }

    /**
     * 累加明细完成数量，达到明细数量时自动标记该明细完成
     *
     * @param detailId 明细 ID
     * @param quantity 完成数量
     */
    public final void addFinishQuantity(long detailId, double quantity) {
        transactionHelper.run(() -> updateWithLock(detailId, detail -> {
            FORBIDDEN.when(detail.getIsFinished(), "该明细已标记完成，无法再添加明细完成数量");
            double finishQuantity = NumberUtil.add(detail.getFinishQuantity(), quantity);
            detail.setFinishQuantity(finishQuantity).setIsFinished(finishQuantity >= detail.getQuantity());
            log.info("添加完成数量:{} 是否完成:{}", finishQuantity, detail.getIsFinished());
        }));
    }

    /**
     * 把一次产量按明细逐行分配累加
     *
     * @param billId      单据 ID
     * @param quantity    本次分配数量
     * @param billService 单据 Service，提供加锁与状态推进
     * @param detailCheck 明细筛选函数，抛异常表示该行不匹配本次分配
     * @apiNote 加锁单据行必须在第一次读明细之前，与
     * {@code AbstractBaseBillService#addDetailFinishQuantity} 保持相同的加锁顺序，
     * 否则并发下两处会互相死等。被跳过的明细只告警不报错，产量会静默丢失
     */
    public <B extends AbstractBaseBillEntity<B, ?>, BS extends AbstractBaseBillService<B, ?, ?, ?, ?>> void updateDetailQuantity(
            long billId,
            double quantity,
            @NotNull BS billService,
            Consumer<E> detailCheck
    ) {
        PARAM_INVALID.when(quantity < 0, "完成数量不能为负数");
        transactionHelper.run(() -> {
            // 先锁单据行，与 addDetailFinishQuantity 的加锁顺序一致
            billService.getForUpdate(billId);
            // 本次待分配的数量，逐行递减分配，避免同一单据多行相同物料时重复计入
            double remain = quantity;
            for (E detail : getAllByBillId(billId)) {
                if (remain <= 0) {
                    break;
                }
                if (Boolean.TRUE.equals(detail.getIsFinished())) {
                    continue;
                }
                try {
                    detailCheck.accept(detail);
                } catch (Exception e) {
                    // 该明细不匹配本次数量，跳过；必须留痕，否则上层无法察觉产量被丢弃
                    log.warn("明细不匹配本次数量分配，已跳过，明细ID:{} 原因:{}",
                            detail.getId(), e.getMessage());
                    continue;
                }

                double finished = Objects.requireNonNullElse(detail.getFinishQuantity(), 0D);
                double detailNeedQuantity = NumberUtil.subtract(detail.getQuantity(), finished);
                if (detailNeedQuantity <= 0) {
                    continue;
                }
                // 实际分配到本行的数量，不得超过其待完成量
                double applied = Math.min(detailNeedQuantity, remain);
                double newFinishQuantity = NumberUtil.add(finished, applied);
                detail.setFinishQuantity(newFinishQuantity)
                        .setIsFinished(newFinishQuantity >= detail.getQuantity());
                updateToDatabase(detail);
                remain = NumberUtil.subtract(remain, applied);
            }
            if (isAllDetailFinished(billId)) {
                billService.setBillDetailsAllFinished(billId);
            }
        });
    }
}
