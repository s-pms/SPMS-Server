package cn.hamm.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.mes.operation.CraftOperationEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zfy
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Description("工艺路线工序")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "craft_router_operation")
@EqualsAndHashCode(callSuper = true)
public class CraftRouterOperationEntity extends BaseEntity<CraftRouterOperationEntity> {

    @Payload
    @Description("工序")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工序不能为空")
    private CraftOperationEntity operation;

    @Description("排序号")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '排序号'")
    @Exclude(filters = {WhenPayLoad.class})
    private Integer orderNo;

}
