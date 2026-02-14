package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.curd.query.QueryListRequest;
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
    protected void beforeDelete(@NotNull StorageEntity storage) {
        TreeUtil.ensureNoChildrenBeforeDelete(storage.getId(), id -> filter(new StorageEntity().setParentId(id)));
    }

    @Override
    protected @NotNull QueryListRequest<StorageEntity> beforeGetList(@NotNull QueryListRequest<StorageEntity> queryListRequest) {
        StorageEntity filter = queryListRequest.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setParentId(TreeUtil.ROOT_ID);
        }
        queryListRequest.setFilter(filter);
        return queryListRequest;
    }
}
