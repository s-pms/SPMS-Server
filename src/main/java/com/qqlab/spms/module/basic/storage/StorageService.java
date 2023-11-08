package com.qqlab.spms.module.basic.storage;

import cn.hamm.airpower.query.QueryRequest;
import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class StorageService extends BaseService<StorageEntity, StorageRepository> {
    @Override
    protected List<StorageEntity> afterGetList(List<StorageEntity> list) {
        for (StorageEntity item : list) {
            QueryRequest<StorageEntity> queryRequest = new QueryRequest<>();
            queryRequest.setFilter(new StorageEntity().setParentId(item.getId()));
            item.setChildren(this.getList(queryRequest));
        }
        return list;
    }

    @Override
    protected QueryRequest<StorageEntity> beforeGetList(QueryRequest<StorageEntity> queryRequest) {
        StorageEntity filter = queryRequest.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setParentId(0L);
        }
        return queryRequest.setFilter(filter);
    }
}