package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.interfaces.ITree;
import cn.hamm.airpower.model.query.QueryPageRequest;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.spms.base.BaseEntity;
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
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class InventoryService extends BaseService<InventoryEntity, InventoryRepository> {
    private final StorageService storageService;
    private final StructureService structureService;

    public InventoryService(StorageService storageService, StructureService structureService) {
        super();
        this.storageService = storageService;
        this.structureService = structureService;
    }

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
        return repository.getByMaterialAndStructure(
                new MaterialEntity().setId(materialId), new StructureEntity().setId(structureId)
        );
    }

    @Override
    protected @NotNull QueryPageRequest<InventoryEntity> beforeGetPage(@NotNull QueryPageRequest<InventoryEntity> sourceRequestData) {
        return super.beforeGetPage(sourceRequestData);
    }

    @Override
    protected InventoryEntity beforeCreatePredicate(@NotNull InventoryEntity filter) {
        // 需要移除本身的查询条件
        switch (DictionaryUtil.getDictionary(InventoryType.class, filter.getType())) {
            case STORAGE -> filter.setStorage(null);
            case STRUCTURE -> filter.setStructure(null);
            default -> {
            }
        }
        return filter;
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
                Set<Long> idList = getIdList(search.getStorage().getId(), storageService, StorageEntity.class);
                if (!idList.isEmpty()) {
                    Join<InventoryEntity, StorageEntity> join = root.join("storage");
                    Predicate inPredicate = join.get(Constant.ID).in(idList);
                    predicateList.add(inPredicate);
                }
            }
            case STRUCTURE -> {
                if (Objects.isNull(search.getStructure())) {
                    return predicateList;
                }
                Set<Long> idList = getIdList(search.getStorage().getId(), structureService, StructureEntity.class);
                if (!idList.isEmpty()) {
                    Join<InventoryEntity, StructureEntity> join = root.join("structure");
                    Predicate inPredicate = join.get(Constant.ID).in(idList);
                    predicateList.add(inPredicate);
                }
            }
            default -> {
            }
        }
        return predicateList;
    }

    private <T extends BaseEntity<T> & ITree<T>> @NotNull Set<Long> getIdList(
            long parentId,
            @NotNull BaseService<T, ?> service,
            @NotNull Class<T> entityClass
    ) {
        Set<Long> list = new HashSet<>();
        getIdList(parentId, service, entityClass, list);
        return list;
    }

    private <T extends BaseEntity<T> & ITree<T>> void getIdList(
            long parentId,
            @NotNull BaseService<T, ?> service,
            @NotNull Class<T> entityClass,
            @NotNull Set<Long> list
    ) {
        T parent = service.get(parentId);
        list.add(parent.getId());
        List<T> children;
        try {
            children = service.filter(entityClass.getConstructor().newInstance().setParentId(parent.getId()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        children.forEach(child -> getIdList(child.getId(), service, entityClass, list));
    }
}
