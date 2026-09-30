package cn.hamm.spms.module.wms.input.detail;

import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.ReadOnly;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static jakarta.persistence.FetchType.EAGER;

/**
 * <h1>入库明细实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "input_detail")
@Description("入库明细")
public class InputDetailEntity extends BaseBillDetailEntity<InputDetailEntity> {
    @Description("物料信息")
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("入库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '入库数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "入库数量不能为空")
    @Meta
    private Double quantity;

    @Description("已入库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已入库数量'")
    @Meta
    @ReadOnly
    private Double finishQuantity;

    @Description("仓库")
    // 必须是真正的持久化关联：原先是 @Transient，导致入库时只能取客户端传的仓库，
    // 既可以被任意篡改，又在数据库里留不下任何痕迹。落库后报工时以数据库明细为准。
    @ManyToOne(fetch = EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "入库仓库不能为空")
    private StorageEntity storage;
}
