package com.qqlab.spms.module.system.menu;

import cn.hamm.airpower.model.Sort;
import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.result.Result;
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
public class MenuService extends BaseService<MenuEntity, MenuRepository> {
    @Override
    public void delete(Long id) {
        QueryRequest<MenuEntity> queryRequest = new QueryRequest<>();
        queryRequest.setFilter(new MenuEntity().setParentId(id));
        List<MenuEntity> children = getList(queryRequest);
        Result.FORBIDDEN_DELETE.when(!children.isEmpty(), "含有子菜单,无法删除!");
        deleteById(id);
    }

    @Override
    protected QueryRequest<MenuEntity> beforeGetList(QueryRequest<MenuEntity> queryRequest) {
        MenuEntity filter = queryRequest.getFilter();
        if (Objects.isNull(queryRequest.getSort())) {
            queryRequest.setSort(new Sort().setField("orderNo"));
        }
        return queryRequest.setFilter(filter);
    }

    @Override
    protected List<MenuEntity> afterGetList(List<MenuEntity> list) {
        for (MenuEntity item : list) {
            item.excludeBaseData();
        }
        return list;
    }
}
