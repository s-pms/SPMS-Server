package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

/**
 * <h1>采购单实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "sale")
@Description("销售单")
public class SaleEntity extends AbstractBaseBillEntity<SaleDetailEntity> {
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
    @Dictionary(value = SaleStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer status;

    @Description("客户信息")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "客户不能为空")
    private CustomerEntity customer;
}
