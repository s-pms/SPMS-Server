package cn.hamm.spms.base.bill.detail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * <h1>单据明细基类</h1>
 *
 * @param <E> 明细实体
 * @author Hamm.cn
 */
@SuppressWarnings({"unchecked", "UnusedReturnValue"})
@MappedSuperclass
@Getter
@Description("")
public abstract class BaseBillDetailEntity<E extends BaseBillDetailEntity<E>> extends BaseEntity<E> implements IBaseBillDetailAction {
    @Description("单据ID")
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '单据ID'")
    private Long billId;

    @Description("是否已完成")
    @Search(Search.Mode.EQUALS)
    @ReadOnly
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否已完成'")
    private Boolean isFinished;

    /**
     * <h3>设置是否已完成</h3>
     *
     * @param isFinished 是否已完成
     * @return 单据明细
     */
    public E setIsFinished(Boolean isFinished) {
        this.isFinished = isFinished;
        return (E) this;
    }

    /**
     * <h3>设置单据ID</h3>
     *
     * @param billId 单据ID
     * @return 明细实体
     */
    public E setBillId(Long billId) {
        this.billId = billId;
        return (E) this;
    }

    /**
     * <h3>获取数量</h3>
     *
     * @return 数量
     */
    public abstract Double getQuantity();

    /**
     * <h3>设置数量</h3>
     *
     * @param quantity 数量
     * @return 明细实体
     */
    public abstract E setQuantity(Double quantity);

    /**
     * <h3>获取已完成数量</h3>
     *
     * @return 数量
     */
    public abstract Double getFinishQuantity();

    /**
     * <h3>设置已完成数量</h3>
     *
     * @param finishQuantity 已完成数量
     * @return 明细实体
     */
    public abstract E setFinishQuantity(Double finishQuantity);
}
