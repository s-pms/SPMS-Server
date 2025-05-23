package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.curd.export.ExcelColumn;
import cn.hamm.airpower.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.mes.bom.BomEntity;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;
import static cn.hamm.airpower.curd.export.ExportColumnType.BOOLEAN;
import static cn.hamm.spms.module.system.coderule.CodeRuleField.RoutingCode;
import static jakarta.persistence.FetchType.EAGER;

/**
 * <h1>实体</h1>
 *
 * @author zfy
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "routing")
@Description("生产工艺")
public class RoutingEntity extends BaseEntity<RoutingEntity> {
    @Description("工艺编码")
    @Column(columnDefinition = "varchar(255) default '' comment '工艺编码'", unique = true)
    @AutoGenerateCode(RoutingCode)
    private String code;

    @Description("工艺名称")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工艺名称不能为空")
    @Column(columnDefinition = "varchar(255) default '' comment '工艺名称'")
    private String name;

    @Description("关联物料")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "关联物料")
    private MaterialEntity material;

    @Description("工艺状态")
    @Search(EQUALS)
    @Dictionary(value = RoutingStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    @ReadOnly
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '工艺状态'")
    private Integer status;

    @Description("BOM信息")
    @ManyToOne(fetch = EAGER)
    private BomEntity bom;

    @Description("使用工艺BOM")
    @Search(EQUALS)
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '使用工艺BOM'")
    @ExcelColumn(BOOLEAN)
    private Boolean isRoutingBom;

    @Description("工序配置列表")
    @Transient
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工序配置列表不能为空")
    private List<RoutingOperationEntity> details = new ArrayList<>();
}
