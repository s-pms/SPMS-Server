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
     * <h3>查询指定物料在指定仓库的库存</h3>
     *
     * @param material 物料
     * @param storage  仓库
     * @return 库存
     */
    InventoryEntity getByMaterialAndStorage(MaterialEntity material, StorageEntity storage);

    /**
     * <h3>查询指定物料在指定生产单元下的库存</h3>
     *
     * @param material  物料
     * @param structure 生产单元
     * @return 库存
     */
    InventoryEntity getByMaterialAndStructure(MaterialEntity material, StructureEntity structure);
}
