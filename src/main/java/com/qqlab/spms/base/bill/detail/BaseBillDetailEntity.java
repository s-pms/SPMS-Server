package com.qqlab.spms.base.bill.detail;

import cn.hamm.airpower.annotation.Description;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qqlab.spms.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * <h1>单据明细基类</h1>
 *
 * @param <D> 明细实体
 * @author hamm
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@MappedSuperclass
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamicInsert
@DynamicUpdate
@Description("")
public class BaseBillDetailEntity<D extends BaseBillDetailEntity<D>> extends BaseEntity<D> {
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '单据号'")
    private Long billId;

    /**
     * <h2>设置单据ID</h2>
     *
     * @param billId 单据ID
     * @return 明细实体
     */
    public D setBillId(Long billId) {
        this.billId = billId;
        return (D) this;
    }
}
