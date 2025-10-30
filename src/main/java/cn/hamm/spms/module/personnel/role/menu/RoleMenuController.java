package cn.hamm.spms.module.personnel.role.menu;

import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.curd.Sort;
import cn.hamm.airpower.curd.query.QueryListRequest;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.system.menu.MenuService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("role/menu")
@Description("角色菜单")
public class RoleMenuController extends BaseController<RoleMenuEntity, RoleMenuService, RoleMenuRepository> {
    @Permission(authorize = false)
    @Override
    public Json getList(QueryListRequest<RoleMenuEntity> queryListRequest) {
        return super.getList(queryListRequest);
    }

    @Override
    protected QueryListRequest<RoleMenuEntity> beforeGetList(@NotNull QueryListRequest<RoleMenuEntity> queryListRequest) {
        RoleMenuEntity filter = queryListRequest.getFilter();
        queryListRequest.setSort(Objects.requireNonNullElse(
                queryListRequest.getSort(),
                new Sort().setField(MenuService.ORDER_FIELD_NAME)
        ));
        queryListRequest.setFilter(filter);
        return queryListRequest;
    }
}
