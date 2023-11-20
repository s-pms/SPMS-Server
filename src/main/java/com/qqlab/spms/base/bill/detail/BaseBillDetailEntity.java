package com.qqlab.spms.base.bill.detail;

import cn.hamm.airpower.annotation.Description;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qqlab.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


/**
 * <h1>单据明细基类</h1>
 *
 * @param <E> 明细实体
 * @author hamm
 * @noinspection unchecked
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@MappedSuperclass
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamicInsert
@DynamicUpdate
@Description("")
public abstract class BaseBillDetailEntity<E extends BaseBillDetailEntity<E>> extends BaseEntity<E> {
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '单据号'")
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
    public E addFinishQuantity(Double quantity) {
        this.setFinishQuantity(this.getFinishQuantity() + quantity);
        return (E) this;
    }

    /**
     * <h2>添加完成数量</h2>
     */
    public interface WhenAddFinish {
    }
}
