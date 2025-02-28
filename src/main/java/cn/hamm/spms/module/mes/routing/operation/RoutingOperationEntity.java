package cn.hamm.spms.module.mes.routing.operation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.mes.bom.BomEntity;
import cn.hamm.spms.module.mes.operation.OperationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;
import static cn.hamm.airpower.annotation.Search.Mode.JOIN;
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
@Table(name = "routing_operation")
@Description("工序配置")
public class RoutingOperationEntity extends BaseEntity<RoutingOperationEntity> {
    @Description("工艺ID")
    @Column(nullable = false, columnDefinition = "bigint UNSIGNED comment '工艺ID'")
    private Long routingId;

    @Description("工序信息")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工序信息")
    @Search(JOIN)
    private OperationEntity operation;

    @Description("BOM信息")
    @ManyToOne(fetch = EAGER)
    @Search(JOIN)
    private BomEntity bom;

    @Description("排序号")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '排序号'")
    private Integer orderNo;

    @Description("是否自动流转")
    @Search(EQUALS)
    @ReadOnly
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否自动流转'")
    private Boolean isAutoNext;
}
