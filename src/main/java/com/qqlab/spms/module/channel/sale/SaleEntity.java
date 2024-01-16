package com.qqlab.spms.module.channel.sale;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.common.annotation.AutoGenerateCode;
import com.qqlab.spms.module.channel.customer.CustomerEntity;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

/**
 * <h1>采购单实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "sale")
@Description("销售单")
public class SaleEntity extends AbstractBaseBillEntity<SaleEntity, SaleDetailEntity> {
    @Description("销售单号")
    @Column(columnDefinition = "varchar(255) default '' comment '销售单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.SaleBillCode)
    @Search(Search.Mode.LIKE)
    private String billCode;

    @Description("销售说明")
    @Column(columnDefinition = "varchar(255) default '' comment '销售说明'")
    @Length(max = 80, message = "采购事由仅支持输入{min}个{max}个字符")
    @Search(Search.Mode.LIKE)
    private String reason;

    @Description("总金额")
    @ReadOnly
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '总金额'")
    private Double totalPrice;

    @Description("销售状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '销售状态'")
    @Dictionary(SaleStatus.class)
    @Search(Search.Mode.EQUALS)
    private Integer status;

    /**
     * 客户信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "客户不能为空")
    private CustomerEntity customer;
}
