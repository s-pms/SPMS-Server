package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class StructureService extends BaseService<StructureEntity, StructureRepository> {

    /**
     * 层级深度上限，超过则判定为历史脏数据（正常业务远达不到）
     */
    private static final int MAX_TREE_DEPTH = 32;

    /**
     * 一次性把列表组装成树
     * <p>
     * 原实现在这里递归调用 {@code getList} 逐层查库，既是 N+1 查询，
     * 又会在父子关系成环时无限递归直接爆栈。改用框架的
     * {@code TreeUtil.buildTreeList}：单遍 Map 组装，O(n)，不递归。
     * </p>
     *
     * @param list 生产单元列表
     * @return 树形结构
     */
    @Override
    protected @NotNull List<StructureEntity> afterGetList(@NotNull List<StructureEntity> list) {
        return TreeUtil.buildTreeList(list);
    }

    @Override
    protected void beforeDelete(@NotNull StructureEntity structure) {
        TreeUtil.ensureNoChildrenBeforeDelete(structure.getId(), id -> filter(new StructureEntity().setParentId(id)));
    }

    @Override
    protected @NotNull StructureEntity beforeAppSaveToDatabase(@NotNull StructureEntity structure) {
        Long selfId = structure.getId();
        Long parentId = structure.getParentId();
        if (Objects.isNull(selfId)) {
            return structure;
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
            cursor = filter(new StructureEntity().setId(current)).stream()
                    .findFirst()
                    .map(StructureEntity::getParentId)
                    .orElse(null);
        }
        return structure;
    }
}
