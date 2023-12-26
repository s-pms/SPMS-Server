package com.qqlab.spms.base.bill;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Payload;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>单据实体基类</h1>
 *
 * @param <E> 单据实体
 * @param <D> 明细实体
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@MappedSuperclass
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamicInsert
@DynamicUpdate
@Description("")
public abstract class AbstractBaseBillEntity<E extends AbstractBaseBillEntity<E, D>, D extends BaseBillDetailEntity<D>> extends BaseEntity<E> {
    /**
     * 单据明细
     */
    @Payload
    @Transient
    @Exclude(filters = {WhenPayLoad.class})
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "单据明细不能为空")
    private List<D> details = new ArrayList<>();

    @Description("驳回原因")
    @NotNull(groups = {WhenReject.class}, message = "驳回原因")
    @Column(columnDefinition = "varchar(255) default '' comment '驳回原因'")
    @Length(max = 80, message = "驳回原因仅支持{max}个字符")
    private String rejectReason;


    /**
     * 设置驳回原因
     *
     * @param rejectReason 驳回原因
     * @return 单据实体
     */
    @SuppressWarnings("UnusedReturnValue")
    public E setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
        //noinspection unchecked
        return (E) this;
    }


    /**
     * 设置单据明细
     *
     * @param details 明细
     * @return 单据实体
     */
    public E setDetails(List<D> details) {
        this.details = details;
        //noinspection unchecked
        return (E) this;
    }

    public interface WhenReject {
    }

    /**
     * 设置状态
     *
     * @param status 状态值
     * @return 单据实体
     */
    public abstract E setStatus(Integer status);
}
