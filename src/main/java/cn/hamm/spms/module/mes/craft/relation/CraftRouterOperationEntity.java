package cn.hamm.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.mes.operation.OperationEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
@Table(name = "craft_router_operation")
@Description("工艺路线工序")
public class CraftRouterOperationEntity extends BaseEntity<CraftRouterOperationEntity> {
    @Payload
    @Description("工序")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工序不能为空")
    private OperationEntity operation;

    @Description("排序号")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '排序号'")
    private Integer orderNo;

}
