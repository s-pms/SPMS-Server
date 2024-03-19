package cn.hamm.spms.module.wms.input.detail;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
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
 * <h1>入库明细实体</h1>
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
@Table(name = "input_detail")
@Description("入库明细")
public class InputDetailEntity extends BaseBillDetailEntity<InputDetailEntity> {
    /**
     * 存储资源
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    private StorageEntity storage;

    /**
     * 物料信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("入库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '入库数量'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "入库数量不能为空")
    private Double quantity;

    @Description("已入库数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '已入库数量'")
    private Double finishQuantity;
}
