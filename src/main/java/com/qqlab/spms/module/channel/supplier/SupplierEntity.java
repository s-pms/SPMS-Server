package com.qqlab.spms.module.channel.supplier;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.validate.phone.Phone;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.common.annotation.AutoGenerateCode;
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
@Table(name = "supplier")
@Description("供应商")
public class SupplierEntity extends BaseEntity<SupplierEntity> {
    @Description("供应商名称")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "供应商名称不能为空")
    @Column(columnDefinition = "varchar(255) default '' comment '供应商名称'")
    private String name;

    @Description("供应商编码")
    @Column(columnDefinition = "varchar(255) default '' comment '供应商编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.SupplierCode)
    private String code;

    @Description("联系电话")
    @Column(columnDefinition = "varchar(255) default '' comment '联系电话'")
    @Phone
    private String phone;
}
