package cn.hamm.spms.module.mes.bom.detail;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
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

import static jakarta.persistence.FetchType.EAGER;

/**
 * <h1>BOM 明细实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "bom_detail")
@Description("BOM 明细")
public class BomDetailEntity extends BaseEntity<BomDetailEntity> {
    @Description("物料信息")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "数量不能为空")
    private Double quantity;
}
