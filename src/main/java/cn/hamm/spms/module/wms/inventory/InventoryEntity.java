package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;
import static cn.hamm.airpower.annotation.Search.Mode.JOIN;
import static jakarta.persistence.FetchType.EAGER;

/**
 * <h1>库存实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "inventory")
@Description("库存")
public class InventoryEntity extends BaseEntity<InventoryEntity> {
    @Description("物料信息")
    @ManyToOne(fetch = EAGER)
    @Search(JOIN)
    private MaterialEntity material;

    @Description("库存数量")
    @Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '库存数量'")
    private Double quantity;

    @Description("存储类型")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '存储类型'")
    @Dictionary(value = InventoryType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Search(EQUALS)
    private Integer type;

    @Description("仓库")
    @ManyToOne(fetch = EAGER)
    @Search(JOIN)
    private StorageEntity storage;

    @Description("生产单元")
    @ManyToOne(fetch = EAGER)
    @Search(JOIN)
    private StructureEntity structure;
}
