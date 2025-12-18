package cn.hamm.spms.module.wms.input.detail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.meta.Meta;
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
    private Double finishQuantity;

    @Description("仓库")
    @Transient
    private StorageEntity storage;
}
