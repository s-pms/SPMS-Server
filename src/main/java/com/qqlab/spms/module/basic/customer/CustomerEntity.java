package com.qqlab.spms.module.basic.customer;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.validate.phone.Phone;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * <h1>供应商实体</h1>
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
@Table(name = "customer")
@Description("客户")
public class CustomerEntity extends BaseEntity<CustomerEntity> {
    @Description("客户名称")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "客户名称不能为空")
    @Column(columnDefinition = "varchar(255) default '' comment '客户名称'")
    private String name;

    @Description("客户编码")
    @Column(columnDefinition = "varchar(255) default '' comment '客户编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.CustomerCode)
    private String code;

    @Description("联系电话")
    @Column(columnDefinition = "varchar(255) default '' comment '联系电话'")
    @Phone
    private String phone;
}
