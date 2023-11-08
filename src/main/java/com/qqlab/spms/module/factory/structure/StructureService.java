package com.qqlab.spms.module.factory.structure;

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
public class StructureService extends BaseService<StructureEntity, StructureRepository> {
    @Override
    protected List<StructureEntity> afterGetList(List<StructureEntity> list) {
        for (StructureEntity item : list) {
            QueryRequest<StructureEntity> queryRequest = new QueryRequest<>();
            queryRequest.setFilter(new StructureEntity().setParentId(item.getId()));
            item.setChildren(this.getList(queryRequest));
        }
        return list;
    }

    @Override
    protected QueryRequest<StructureEntity> beforeGetList(QueryRequest<StructureEntity> queryRequest) {
        StructureEntity filter = queryRequest.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setParentId(0L);
        }
        return queryRequest.setFilter(filter);
    }
}