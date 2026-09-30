package cn.hamm.spms.base.bill.detail;

import cn.hamm.airpower.core.NumberUtil;
import jakarta.persistence.LockModeType;
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
 * <h1>单据明细 Service 基类</h1>
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
     * <p>
     * 逐条走 {@code delete(id)} 以触发钩子。不用 {@code repository.deleteByBillId}
     * 这类派生批量删除，它不触发 JPA 生命周期回调也不做级联。
     * </p>
     *
     * @param billId 单据 ID
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
     * <p>
     * 普通读即可，但调用方必须已持有单据行锁：本方法要是提前到加锁之前读，
     * 就会固定住旧读视图，导致并发下误判「未全部完成」而卡死。
     * </p>
     * <p>
     * 空集合判否：{@code allMatch} 在空集合上返回 true，
     * 会让明细被清空的单据被判定为全部完成，连锁生成 0 明细的下游单据。
     * </p>
     *
     * @param billId 单据 ID
     * @return true 表示全部明细都已完成
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
     * @param details 明细
     */
    public final void saveDetails(long billId, @NotNull List<E> details) {
        transactionHelper.run(() -> {
            deleteAllByBillId(billId);
            details.forEach(detail -> add(detail.setBillId(billId)));
        });
    }

    /**
     * 添加完成数量
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
     * 更新明细的数量
     *
     * @param billId      单据 ID
     * @param quantity    本次更新数量
     * @param billService 单据 Service
     * @param detailCheck 明细检查函数
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
            billService.getBillForUpdate(billId);
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
                // 该明细还需要完成的数量
                double detailNeedQuantity = NumberUtil.subtract(detail.getQuantity(), finished);
                if (detailNeedQuantity <= 0) {
                    continue;
                }
                // 实际分配到本行的数量，不得超过其待完成量
                double applied = Math.min(detailNeedQuantity, remain);
                // 累加而非覆盖
                double newFinishQuantity = NumberUtil.add(finished, applied);
                detail.setFinishQuantity(newFinishQuantity)
                        .setIsFinished(newFinishQuantity >= detail.getQuantity());
                updateToDatabase(detail);
                remain = NumberUtil.subtract(remain, applied);
            }
            if (isAllDetailFinished(billId)) {
                // 明细已全部完成
                billService.setBillDetailsAllFinished(billId);
            }
        });
    }
}
