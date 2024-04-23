package cn.hamm.spms.module.wms.inventory;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface InventoryRepository extends BaseRepository<InventoryEntity> {
    /**
     * <h2>查询指定物料在指定存储资源的库存</h2>
     *
     * @param material 物料
     * @param storage  存储资源
     * @return 库存
     */
    InventoryEntity getByMaterialAndStorage(MaterialEntity material, StorageEntity storage);

    /**
     * <h2>查询指定物料在指定工厂结构下的库存</h2>
     *
     * @param material  物料
     * @param structure 工厂结构
     * @return 库存
     */
    InventoryEntity getByMaterialAndStructure(MaterialEntity material, StructureEntity structure);
}
