package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.web.curd.query.QueryListRequest;
import cn.hamm.airpower.web.tree.TreeUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class StructureService extends BaseService<StructureEntity, StructureRepository> {
    @Override
    protected @NotNull List<StructureEntity> afterGetList(@NotNull List<StructureEntity> list) {
        return TreeUtil.getAllChildren(this, list);
    }

    @Override
    protected void beforeDelete(@NotNull StructureEntity structure) {
        TreeUtil.ensureNoChildrenBeforeDelete(this, structure.getId());
    }

    @Override
    protected @NotNull QueryListRequest<StructureEntity> beforeGetList(@NotNull QueryListRequest<StructureEntity> queryListRequest) {
        StructureEntity filter = queryListRequest.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setParentId(TreeUtil.ROOT_ID);
        }
        queryListRequest.setFilter(filter);
        return queryListRequest;
    }
}
