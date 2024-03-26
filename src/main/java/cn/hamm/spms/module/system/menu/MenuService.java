package cn.hamm.spms.module.system.menu;

import cn.hamm.airpower.model.Sort;
import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.result.Result;
import cn.hamm.spms.base.BaseService;
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
    protected void beforeDelete(long id) {
        QueryRequest<MenuEntity> queryRequest = new QueryRequest<>();
        queryRequest.setFilter(new MenuEntity().setParentId(id));
        List<MenuEntity> children = getList(queryRequest);
        Result.FORBIDDEN_DELETE.when(!children.isEmpty(), "含有子菜单,无法删除!");
    }

    @Override
    protected <T extends QueryRequest<MenuEntity>> T beforeGetList(T sourceRequestData) {
        MenuEntity filter = sourceRequestData.getFilter();
        if (Objects.isNull(sourceRequestData.getSort())) {
            sourceRequestData.setSort(new Sort().setField("orderNo"));
        }
        sourceRequestData.setFilter(filter);
        return sourceRequestData;
    }

    @Override
    protected List<MenuEntity> afterGetList(List<MenuEntity> list) {
        for (MenuEntity item : list) {
            item.excludeBaseData();
        }
        return list;
    }
}
