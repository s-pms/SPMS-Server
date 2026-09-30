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
     *
     * @param billId 单据 ID
     */
    public final void deleteAllByBillId(Long billId) {
        transactionHelper.run(() -> {
            List<E> details = getAllByBillId(billId);
            details.forEach(detail -> repository.deleteById(detail.getId()));
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
                    // 该明细不匹配本次数量，跳过；必须留下痕迹，否则上层无法察觉产量被静默丢弃
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
            // 判断所有明细是否完成
            List<E> details = getAllByBillId(billId);
            boolean isAllFinished = details.stream().allMatch(BaseBillDetailEntity::getIsFinished);
            if (isAllFinished) {
                // 明细已全部完成
                billService.setBillDetailsAllFinished(billId);
            }
        });
    }
}
