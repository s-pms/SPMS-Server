package cn.hamm.spms.base.bill.detail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * <h1>单据明细基类</h1>
 *
 * @param <E> 明细实体
 * @author Hamm
 */
@SuppressWarnings({"unchecked", "UnusedReturnValue"})
@MappedSuperclass
@Getter
@Description("")
public abstract class BaseBillDetailEntity<E extends BaseBillDetailEntity<E>> extends BaseEntity<E> {
    @Description("单据ID")
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '单据ID'")
    private Long billId;

    /**
     * <h2>设置单据ID</h2>
     *
     * @param billId 单据ID
     * @return 明细实体
     */
    public E setBillId(Long billId) {
        this.billId = billId;
        return (E) this;
    }

    /**
     * <h2>获取数量</h2>
     *
     * @return 数量
     */
    public abstract Double getQuantity();

    /**
     * <h2>设置数量</h2>
     *
     * @param quantity 数量
     * @return 明细实体
     */
    public abstract E setQuantity(Double quantity);

    /**
     * <h2>获取已完成数量</h2>
     *
     * @return 数量
     */
    public abstract Double getFinishQuantity();

    /**
     * <h2>设置已完成数量</h2>
     *
     * @param finishQuantity 已完成数量
     * @return 明细实体
     */
    public abstract E setFinishQuantity(Double finishQuantity);

    /**
     * <h2>添加完成数量</h2>
     *
     * @param quantity 数量
     * @return 明细实体
     */
    @SuppressWarnings("unused")
    public E addFinishQuantity(Double quantity) {
        this.setFinishQuantity(this.getFinishQuantity() + quantity);
        return (E) this;
    }
}
