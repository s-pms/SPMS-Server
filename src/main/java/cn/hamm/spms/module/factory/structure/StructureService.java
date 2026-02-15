package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.curd.model.query.QueryListRequest;
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
        list.forEach(item -> {
            QueryListRequest<StructureEntity> queryListRequest = new QueryListRequest<>();
            queryListRequest.setFilter(new StructureEntity().setParentId(item.getId()));
            item.setChildren(getList(queryListRequest));
        });
        return list;
    }

    @Override
    protected void beforeDelete(@NotNull StructureEntity structure) {
        TreeUtil.ensureNoChildrenBeforeDelete(structure.getId(), id -> filter(new StructureEntity().setParentId(id)));
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
