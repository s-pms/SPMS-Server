package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>仓库</h1>
 *
 * @author Hamm.cn
 */
@Service
public class StorageService extends BaseService<StorageEntity, StorageRepository> {

    /**
     * 层级深度上限，超过则判定为历史脏数据（正常业务远达不到）
     */
    private static final int MAX_TREE_DEPTH = 32;

    /**
     * 把列表一次性组装成树
     *
     * @param list 仓库列表
     * @return 树形结构
     * @apiNote 走 {@code TreeUtil.buildTreeList} 单遍组装：逐层调 {@code getList} 既是 N+1 查询，
     * 父子成环时还会无限递归直接爆栈
     */
    @Override
    protected @NotNull List<StorageEntity> afterGetList(@NotNull List<StorageEntity> list) {
        return TreeUtil.buildTreeList(list);
    }

    @Override
    protected void beforeDelete(@NotNull StorageEntity storage) {
        TreeUtil.ensureNoChildrenBeforeDelete(storage.getId(), id -> filter(new StorageEntity().setParentId(id)));
    }

    @Override
    protected @NotNull StorageEntity beforeAppSaveToDatabase(@NotNull StorageEntity storage) {
        Long selfId = storage.getId();
        Long parentId = storage.getParentId();
        if (Objects.isNull(selfId)) {
            return storage;
        }
        PARAM_INVALID.when(Objects.equals(selfId, parentId), "父级不能是自己");
        // 沿父链向上走，一旦走回自己就说明会形成环
        Long cursor = parentId;
        int depth = 0;
        while (Objects.nonNull(cursor) && cursor != TreeUtil.ROOT_ID) {
            PARAM_INVALID.when(Objects.equals(cursor, selfId), "父级设置会形成循环层级");
            if (++depth > MAX_TREE_DEPTH) {
                PARAM_INVALID.show("父级层级异常，请检查历史层级数据");
            }
            final long current = cursor;
            cursor = filter(new StorageEntity().setId(current)).stream()
                    .findFirst()
                    .map(StorageEntity::getParentId)
                    .orElse(null);
        }
        return storage;
    }
}
