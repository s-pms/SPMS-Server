package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.model.query.QueryListRequest;
import cn.hamm.airpower.root.delegate.TreeServiceDelegate;
import cn.hamm.airpower.util.TreeUtil;
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
        return TreeServiceDelegate.getAllChildren(this, list);
    }

    @Override
    protected void beforeDelete(long id) {
        TreeServiceDelegate.ensureNoChildrenBeforeDelete(this, id);
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
