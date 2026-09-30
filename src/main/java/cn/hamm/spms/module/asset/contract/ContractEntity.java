package cn.hamm.spms.module.asset.contract;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.curd.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.asset.contract.document.ContractDocumentEntity;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import cn.hamm.spms.module.asset.contract.enums.ContractType;
import cn.hamm.spms.module.asset.contract.participant.ParticipantEntity;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.LinkedHashSet;
import java.util.Set;

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
    @Meta
    private String code;

    @Description("合同名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '合同名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "合同名称不能为空")
    @Meta
    private String name;

    @Description("合同内容")
    @Search
    @Column(columnDefinition = "text comment '合同内容'")
    private String content;

    @Description("开始时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '开始时间'")
    private Long startTime;

    @Description("结束时间")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '结束时间'")
    private Long endTime;

    @Description("合同类型")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '合同类型'")
    @Dictionary(value = ContractType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("合同状态")
    @Column(columnDefinition = "bigint UNSIGNED default 1 comment '合同状态'")
    @Dictionary(value = ContractStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer status;

    /**
     * 附件列表
     * <p>
     * 关联关系由 {@code contract_document_link} 中间表承载
     * （见 {@code ContractDocumentLinkEntity}），本字段仅用于承接前端提交的
     * JSON 与回传读取结果，不参与持久化。
     * </p>
     * <p>
     * 读取时由 {@code ContractService} 组装，写入时由其同步中间表。
     * </p>
     */
    @Description("附件列表")
    @Transient
    private Set<ContractDocumentEntity> documentList = new LinkedHashSet<>();

    /**
     * 参与方列表
     * <p>
     * 关联关系由 {@code contract_participant_link} 中间表承载
     * （见 {@code ContractParticipantLinkEntity}），本字段仅用于承接前端提交的
     * JSON 与回传读取结果，不参与持久化。
     * </p>
     */
    @Description("参与方列表")
    @Transient
    private Set<ParticipantEntity> participantList = new LinkedHashSet<>();
}
