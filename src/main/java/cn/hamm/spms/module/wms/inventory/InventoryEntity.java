package cn.hamm.spms.module.wms.inventory;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>领料单实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@Table(name = "inventory")
@Description("库存")
public class InventoryEntity extends BaseEntity<InventoryEntity> {

    /**
     * 物料信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @Search(Search.Mode.JOIN)
    private MaterialEntity material;

    @Description("库存数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '库存数量'")
    private Double quantity;

    @Description("存储类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '存储类型'")
    @Dictionary(value = InventoryType.class,groups = {WhenAdd.class, WhenUpdate.class})
    @Search(Search.Mode.EQUALS)
    private Integer type;

    /**
     * 存储资源
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @Search(Search.Mode.JOIN)
    private StorageEntity storage;

    /**
     * 工厂结构
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @Search(Search.Mode.JOIN)
    private StructureEntity structure;
}
