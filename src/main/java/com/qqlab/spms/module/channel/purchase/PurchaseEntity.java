package com.qqlab.spms.module.channel.purchase;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "purchase")
@Description("采购单")
public class PurchaseEntity extends AbstractBaseBillEntity<PurchaseEntity, PurchaseDetailEntity> {
    @Description("采购单号")
    @Column(columnDefinition = "varchar(255) default '' comment '采购单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.PurchaseBillCode)
    @Search(Search.Mode.LIKE)
    private String billCode;

    @Description("采购事由")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "采购事由")
    @Column(columnDefinition = "varchar(255) default '' comment '采购事由'")
    @Length(min = 5, max = 80, message = "采购事由仅支持输入{min}个{max}个字符")
    @Search(Search.Mode.LIKE)
    private String reason;

    @Description("总金额")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '总金额'")
    private Double totalPrice;

    @Description("实际金额")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '实际金额'")
    private Double totalRealPrice;

    @Description("采购状态")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '采购状态'")
    @Dictionary(PurchaseStatus.class)
    @Search(Search.Mode.EQUALS)
    private Integer status;
}
