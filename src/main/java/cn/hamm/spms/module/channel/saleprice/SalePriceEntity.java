package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>销售价格实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "sale_price")
@Description("销售价格")
public class SalePriceEntity extends BaseEntity<SalePriceEntity> implements ISalePriceAction {
    @Description("物料信息")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("客户信息")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "客户不能为空")
    private CustomerEntity customer;

    @Description("销售单价")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '销售单价'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "销售单价不能为空")
    private Double price;
}
