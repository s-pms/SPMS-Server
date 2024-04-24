package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.query.QueryRequest;
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
        for (StorageEntity item : list) {
            QueryRequest<StorageEntity> queryRequest = new QueryRequest<>();
            queryRequest.setFilter(new StorageEntity().setParentId(item.getId()));
            item.setChildren(this.getList(queryRequest));
        }
        return list;
    }

    @Override
    protected <T extends QueryRequest<StorageEntity>> @NotNull T beforeGetList(@NotNull T sourceRequestData) {
        StorageEntity filter = sourceRequestData.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setParentId(0L);
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
        QueryRequest<StorageEntity> queryRequest = new QueryRequest<>();
        queryRequest.setFilter(new StorageEntity().setParentId(pid));
        return this.getList(queryRequest);
    }
}