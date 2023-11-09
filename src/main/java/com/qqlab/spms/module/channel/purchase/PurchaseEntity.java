package com.qqlab.spms.module.channel.purchase;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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
public class PurchaseEntity extends BaseEntity<PurchaseEntity> {
    @Description("采购单号")
    @Column(columnDefinition = "varchar(255) default '' comment '采购单号'", unique = true)
    @AutoGenerateCode(CodeRuleField.PurchaseBillCode)
    private String billCode;

    @Description("采购事由")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "采购事由")
    @Column(columnDefinition = "varchar(255) default '' comment '采购事由'")
    @Length(min = 5, max = 80, message = "采购事由仅支持输入{min}个{max}个字符")
    private String reason;

    @Description("总金额")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '总金额'")
    private Double totalPrice;

    @Description("采购状态")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '采购状态'")
    @Dictionary(PurchaseStatus.class)
    private Integer status;

    /**
     * <h2>采购明细</h2>
     */
    @Payload
    @Transient
    @Exclude(filters = {WhenPayLoad.class})
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "采购明细不能为空")
    private List<PurchaseDetailEntity> details = new ArrayList<>();
}
