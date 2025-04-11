package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.enums.CertificateType;
import cn.hamm.spms.common.enums.IdentityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;

/**
 * <h1>合同参与方实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "participant")
@Description("合同参与方")
public class ParticipantEntity extends BaseEntity<ParticipantEntity> {
    @Description("参与方名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '参与方名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "参与方名称不能为空")
    private String name;

    @Description("联系电话")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '联系电话'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "联系电话不能为空")
    private String phone;

    @Description("电子邮箱")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '电子邮箱'")
    private String email;

    @Description("证件号")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '证件号'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "证件号不能为空")
    private String identification;

    @Description("身份类型")
    @Search(EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '身份类型'")
    @Dictionary(value = IdentityType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("证件类型")
    @Search(EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '证件类型'")
    @Dictionary(value = CertificateType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer certificateType;
}