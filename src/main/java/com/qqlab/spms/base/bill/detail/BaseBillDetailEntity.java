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
 */
@SuppressWarnings({"unchecked", "UnusedReturnValue"})
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
     * 设置单据ID
     *
     * @param billId 单据ID
     * @return 明细实体
     */
    public E setBillId(Long billId) {
        this.billId = billId;
        return (E) this;
    }

    /**
     * 获取数量
     *
     * @return 数量
     */
    public abstract Double getQuantity();

    /**
     * 设置数量
     *
     * @param quantity 数量
     * @return 明细实体
     */
    public abstract E setQuantity(Double quantity);

    /**
     * 获取已完成数量
     *
     * @return 数量
     */
    public abstract Double getFinishQuantity();

    /**
     * 设置已完成数量
     *
     * @param finishQuantity 已完成数量
     * @return 明细实体
     */
    public abstract E setFinishQuantity(Double finishQuantity);

    /**
     * 添加完成数量
     *
     * @param quantity 数量
     * @return 明细实体
     */
    @SuppressWarnings("unused")
    public E addFinishQuantity(Double quantity) {
        this.setFinishQuantity(this.getFinishQuantity() + quantity);
        return (E) this;
    }

    /**
     * 添加完成数量
     */
    public interface WhenAddFinish {
    }
}
