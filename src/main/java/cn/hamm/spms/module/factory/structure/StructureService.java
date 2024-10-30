package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.interfaces.IServiceTree;
import cn.hamm.airpower.model.query.QueryListRequest;
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
public class StructureService extends BaseService<StructureEntity, StructureRepository> implements IServiceTree<StructureEntity> {
    @Override
    protected @NotNull List<StructureEntity> afterGetList(@NotNull List<StructureEntity> list) {
        return getAllChildren(list);
    }

    @Override
    protected void beforeDelete(long id) {
        ensureNoChildrenBeforeDelete(id);
    }

    @Override
    protected @NotNull QueryListRequest<StructureEntity> beforeGetList(@NotNull QueryListRequest<StructureEntity> sourceRequestData) {
        StructureEntity filter = sourceRequestData.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setRootTree();
        }
        sourceRequestData.setFilter(filter);
        return sourceRequestData;
    }
}
