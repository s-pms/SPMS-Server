package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.curd.base.CurdEntity;
import cn.hamm.airpower.curd.model.query.QueryPageRequest;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.storage.StorageService;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import cn.hamm.spms.module.factory.structure.StructureService;
import cn.hamm.spms.module.wms.inventory.enums.InventoryType;
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
     * 查询指定物料 ID 和仓库 ID 下的库存
     *
     * @param materialId 物料 ID
     * @param storageId  仓库 ID
     * @return 库存
     */
    public InventoryEntity getByMaterialIdAndStorageId(Long materialId, Long storageId) {
        return repository.getByMaterialAndStorage(
                new MaterialEntity().setId(materialId),
                new StorageEntity().setId(storageId)
        );
    }

    /**
     * 查询指定物料 ID 和生产单元 ID 下的库存
     *
     * @param materialId  物料 ID
     * @param structureId 生产单元 ID
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
            case STORAGE -> addStoragePredicate(root, search, predicateList);
            case STRUCTURE -> addStructurePredicate(root, search, predicateList);
            default -> {
            }
        }
        return predicateList;
    }

    /**
     * 添加生产单元查询条件
     *
     * @param root          ROOT
     * @param search        查询条件
     * @param predicateList 查询条件列表
     */
    private void addStructurePredicate(@NotNull Root<InventoryEntity> root, @NotNull InventoryEntity search, List<Predicate> predicateList) {
        StructureEntity structure = search.getStructure();
        if (Objects.isNull(structure)) {
            return;
        }
        Set<Long> idList = TreeUtil.getChildrenIdList(structure.getId(), id -> structureService.filter(new StructureEntity().setParentId(id)));
        idList.add(structure.getId());
        Join<InventoryEntity, StructureEntity> join = root.join("structure");
        Predicate inPredicate = join.get(CurdEntity.STRING_ID).in(idList);
        predicateList.add(inPredicate);
        search.setStructure(null);
    }

    /**
     * 添加仓库查询条件
     *
     * @param root          ROOT
     * @param search        查询条件
     * @param predicateList 查询条件列表
     */
    private void addStoragePredicate(@NotNull Root<InventoryEntity> root, @NotNull InventoryEntity search, List<Predicate> predicateList) {
        StorageEntity storage = search.getStorage();
        if (Objects.isNull(storage)) {
            return;
        }
        Set<Long> idList = TreeUtil.getChildrenIdList(storage.getId(), id -> storageService.filter(new StorageEntity().setParentId(id)));
        idList.add(storage.getId());
        Join<InventoryEntity, StorageEntity> join = root.join("storage");
        Predicate inPredicate = join.get(CurdEntity.STRING_ID).in(idList);
        predicateList.add(inPredicate);
        search.setStorage(null);
    }
}
