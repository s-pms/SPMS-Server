package com.qqlab.spms.module.wms.inventory;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.factory.storage.StorageEntity;
import com.qqlab.spms.module.factory.structure.StructureEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>领料单实体</h1>
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
@Table(name = "inventory")
@Description("库存")
public class InventoryEntity extends BaseEntity<InventoryEntity> {

    /**
     * <h2>物料信息</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @Search(Search.Mode.JOIN)
    private MaterialEntity material;

    @Description("库存数量")
    @Column(columnDefinition = "double(11, 6) UNSIGNED default 0 comment '库存数量'")
    private Double quantity;

    @Description("存储类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '存储类型'")
    @Dictionary(InventoryType.class)
    @Search(Search.Mode.EQUALS)
    private Integer type;

    /**
     * <h2>存储资源</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @Search(Search.Mode.JOIN)
    private StorageEntity storage;

    /**
     * <h2>工厂结构</h2>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @Search(Search.Mode.JOIN)
    private StructureEntity structure;
}
