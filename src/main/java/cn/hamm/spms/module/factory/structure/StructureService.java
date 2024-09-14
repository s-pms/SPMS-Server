package cn.hamm.spms.module.factory.structure;

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
    protected @NotNull QueryListRequest<StructureEntity> beforeGetList(@NotNull QueryListRequest<StructureEntity> sourceRequestData) {
        StructureEntity filter = sourceRequestData.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setRootTree();
        }
        sourceRequestData.setFilter(filter);
        return sourceRequestData;
    }

    /**
     * <h2>根据父级ID查询子集</h2>
     *
     * @param pid 父ID
     * @return 列表
     */
    public List<StructureEntity> getByPid(Long pid) {
        QueryListRequest<StructureEntity> queryListRequest = new QueryListRequest<>();
        queryListRequest.setFilter(new StructureEntity().setParentId(pid));
        return getList(queryListRequest);
    }
}
