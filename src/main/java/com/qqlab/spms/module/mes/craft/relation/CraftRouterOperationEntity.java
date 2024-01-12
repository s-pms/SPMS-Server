package com.qqlab.spms.module.mes.craft.relation;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.mes.operation.CraftOperationEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
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
@Table(name = "craft_router_opertion")
@EqualsAndHashCode(callSuper = true)
public class CraftRouterOperationEntity extends BaseEntity<CraftRouterOperationEntity> {

    @Payload
    @Description("工序")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "工序不能为空")
    private CraftOperationEntity operation;

}
