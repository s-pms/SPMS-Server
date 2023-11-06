package com.qqlab.spms.module.basic.material;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.basic.unit.UnitEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * <h1>物料实体</h1>
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
@Table(name = "material")
@Description("物料")
public class MaterialEntity extends BaseEntity<MaterialEntity> {
    /**
     * <h2>物料名称</h2>
     */
    @Description("物料名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '物料名称'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "物料名称不能为空")
    private String name;

    /**
     * <h2>规格型号</h2>
     */
    @Description("规格型号")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '规格型号'")
    private String spc;

    /**
     * <h2>规格型号</h2>
     */
    @Description("物料类型")
    @Search(Search.Mode.EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '物料类型'")
    private Integer materialType;

    /**
     * <h2>默认单位</h2>
     */
    @ManyToOne
    @Search(Search.Mode.JOIN)
    @JoinColumn(nullable = false, name = "unit")
    @Payload
    private UnitEntity unitInfo;
}
