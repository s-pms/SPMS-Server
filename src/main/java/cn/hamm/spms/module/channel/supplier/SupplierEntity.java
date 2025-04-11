package cn.hamm.spms.module.channel.supplier;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.Phone;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.spms.module.system.coderule.CodeRuleField.SupplierCode;

/**
 * <h1>供应商实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "supplier")
@Description("供应商")
public class SupplierEntity extends BaseEntity<SupplierEntity> {
    @Description("供应商名称")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "供应商名称不能为空")
    @Column(columnDefinition = "varchar(255) default '' comment '供应商名称'")
    @Search
    private String name;

    @Description("供应商编码")
    @Column(columnDefinition = "varchar(255) default '' comment '供应商编码'", unique = true)
    @AutoGenerateCode(SupplierCode)
    @Search
    private String code;

    @Description("联系电话")
    @Column(columnDefinition = "varchar(255) default '' comment '联系电话'")
    @Phone(groups = {WhenAdd.class, WhenUpdate.class})
    private String phone;
}
