package cn.hamm.spms.module.asset.contract;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ExcelColumn;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.asset.contract.document.ContractDocumentEntity;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import cn.hamm.spms.module.asset.contract.enums.ContractType;
import cn.hamm.spms.module.asset.contract.participant.ParticipantEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;

import static cn.hamm.airpower.annotation.ExcelColumn.Type.DATETIME;
import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;

/**
 * <h1>合同实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "contract")
@Description("合同")
public class ContractEntity extends BaseEntity<ContractEntity> {
    @Description("合同编码")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '合同编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.ContractCode)
    private String code;

    @Description("合同名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '合同名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "合同名称不能为空")
    private String name;

    @Description("合同内容")
    @Search
    @Column(columnDefinition = "text comment '合同内容'")
    private String content;

    @Description("开始时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '开始时间'")
    @ExcelColumn(DATETIME)
    private Long startTime;

    @Description("结束时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '结束时间'")
    @ExcelColumn(DATETIME)
    private Long endTime;

    @Description("合同类型")
    @Search(EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '合同类型'")
    @Dictionary(value = ContractType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("合同状态")
    @Search(EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 1 comment '合同状态'")
    @Dictionary(value = ContractStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer status;

    @Description("附件列表")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ContractDocumentEntity> documentList;

    @Description("参与方列表")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ParticipantEntity> participantList;
}
