package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.root.delegate.TreeServiceDelegate;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class InventoryService extends BaseService<InventoryEntity, InventoryRepository> {
    /**
     * <h3>查询指定物料ID和存储资源ID下的库存</h3>
     *
     * @param materialId 物料ID
     * @param storageId  存储资源ID
     * @return 库存
     */
    public InventoryEntity getByMaterialIdAndStorageId(Long materialId, Long storageId) {
        return repository.getByMaterialAndStorage(new MaterialEntity().setId(materialId), new StorageEntity().setId(storageId));
    }

    /**
     * <h3>查询指定物料ID和工厂结构ID下的库存</h3>
     *
     * @param materialId  物料ID
     * @param structureId 工厂结构ID
     * @return 库存
     */
    @SuppressWarnings("unused")
    public InventoryEntity getByMaterialIdAndStructureId(Long materialId, Long structureId) {
        return repository.getByMaterialAndStructure(new MaterialEntity().setId(materialId), new StructureEntity().setId(structureId));
    }

    /**
     * <h3>查询指定存储结构下的库存</h3>
     *
     * @param storage 存储结构
     * @return 库存列表
     */
    public List<InventoryEntity> getListByStorage(StorageEntity storage) {
        List<InventoryEntity> list;
        if (Objects.isNull(storage)) {
            return filter(new InventoryEntity().setType(InventoryType.STORAGE.getKey()));
        }
        list = filter(new InventoryEntity().setStorage(storage).setType(InventoryType.STORAGE.getKey()));
        List<StorageEntity> storageList = TreeServiceDelegate.findByParentId(Services.getStorageService(), storage.getId());
        storageList.stream()
                .map(this::getListByStorage)
                .forEach(list::addAll);
        return list;
    }

    /**
     * <h3>查询指定工厂结构下的库存</h3>
     *
     * @param structure 工厂结构
     * @return 库存列表
     */
    public List<InventoryEntity> getListByStructure(StructureEntity structure) {
        List<InventoryEntity> list;
        if (Objects.isNull(structure)) {
            return filter(new InventoryEntity().setType(InventoryType.STRUCTURE.getKey()));
        }
        list = filter(new InventoryEntity().setStructure(structure).setType(InventoryType.STRUCTURE.getKey()));
        List<StructureEntity> structureList = TreeServiceDelegate.findByParentId(Services.getStructureService(), structure.getId());
        structureList.stream()
                .map(this::getListByStructure)
                .forEach(list::addAll);
        return list;
    }
}
