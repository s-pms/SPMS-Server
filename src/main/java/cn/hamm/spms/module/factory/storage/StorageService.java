package cn.hamm.spms.module.factory.storage;

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
public class StorageService extends BaseService<StorageEntity, StorageRepository> {
    @Override
    protected @NotNull List<StorageEntity> afterGetList(@NotNull List<StorageEntity> list) {
        list.forEach(item -> {
            QueryListRequest<StorageEntity> queryListRequest = new QueryListRequest<>();
            queryListRequest.setFilter(new StorageEntity().setParentId(item.getId()));
            item.setChildren(getList(queryListRequest));
        });
        return list;
    }

    @Override
    protected <T extends QueryListRequest<StorageEntity>> @NotNull T beforeGetList(@NotNull T sourceRequestData) {
        StorageEntity filter = sourceRequestData.getFilter();
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
    public List<StorageEntity> getByPid(Long pid) {
        QueryListRequest<StorageEntity> queryListRequest = new QueryListRequest<>();
        queryListRequest.setFilter(new StorageEntity().setParentId(pid));
        return getList(queryListRequest);
    }
}
