package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.curd.CurdEntity;
import cn.hamm.airpower.curd.query.QueryPageRequest;
import cn.hamm.airpower.dictionary.DictionaryUtil;
import cn.hamm.airpower.tree.TreeUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.storage.StorageService;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import cn.hamm.spms.module.factory.structure.StructureService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class InventoryService extends BaseService<InventoryEntity, InventoryRepository> {
    @Autowired
    private StorageService storageService;

    @Autowired
    private StructureService structureService;

    /**
     * 查询指定物料ID和仓库ID下的库存
     *
     * @param materialId 物料ID
     * @param storageId  仓库ID
     * @return 库存
     */
    public InventoryEntity getByMaterialIdAndStorageId(Long materialId, Long storageId) {
        return repository.getByMaterialAndStorage(
                new MaterialEntity().setId(materialId),
                new StorageEntity().setId(storageId)
        );
    }

    /**
     * 查询指定物料ID和生产单元ID下的库存
     *
     * @param materialId  物料ID
     * @param structureId 生产单元ID
     * @return 库存
     */
    @SuppressWarnings("unused")
    public InventoryEntity getByMaterialIdAndStructureId(Long materialId, Long structureId) {
        return repository.getByMaterialAndStructure(
                new MaterialEntity().setId(materialId),
                new StructureEntity().setId(structureId)
        );
    }

    @Override
    protected @NotNull QueryPageRequest<InventoryEntity> beforeGetPage(@NotNull QueryPageRequest<InventoryEntity> queryPageRequest) {
        return super.beforeGetPage(queryPageRequest);
    }

    @Override
    protected InventoryEntity beforeCreatePredicate(@NotNull InventoryEntity inventory) {
        // 需要移除本身的查询条件
        switch (DictionaryUtil.getDictionary(InventoryType.class, inventory.getType())) {
            case STORAGE -> inventory.setStorage(null);
            case STRUCTURE -> inventory.setStructure(null);
            default -> {
            }
        }
        return inventory;
    }

    @Override
    protected @NotNull List<Predicate> addSearchPredicate(
            @NotNull Root<InventoryEntity> root,
            @NotNull CriteriaBuilder builder,
            @NotNull InventoryEntity search
    ) {
        List<Predicate> predicateList = new ArrayList<>();
        if (Objects.isNull(search.getType())) {
            return predicateList;
        }
        switch (DictionaryUtil.getDictionary(InventoryType.class, search.getType())) {
            case STORAGE -> {
                if (Objects.isNull(search.getStorage())) {
                    return predicateList;
                }
                Set<Long> idList = TreeUtil.getChildrenIdList(search.getStorage().getId(), storageService, StorageEntity.class);
                if (!idList.isEmpty()) {
                    Join<InventoryEntity, StorageEntity> join = root.join("storage");
                    Predicate inPredicate = join.get(CurdEntity.STRING_ID).in(idList);
                    predicateList.add(inPredicate);
                }
            }
            case STRUCTURE -> {
                if (Objects.isNull(search.getStructure())) {
                    return predicateList;
                }
                Set<Long> idList = TreeUtil.getChildrenIdList(search.getStructure().getId(), structureService, StructureEntity.class);
                if (!idList.isEmpty()) {
                    Join<InventoryEntity, StructureEntity> join = root.join("structure");
                    Predicate inPredicate = join.get(CurdEntity.STRING_ID).in(idList);
                    predicateList.add(inPredicate);
                }
            }
            default -> {
            }
        }
        return predicateList;
    }
}
