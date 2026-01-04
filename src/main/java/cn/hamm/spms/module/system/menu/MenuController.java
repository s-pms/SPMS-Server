package cn.hamm.spms.module.system.menu;

import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.web.access.Permission;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Json;
import cn.hamm.airpower.web.curd.Sort;
import cn.hamm.airpower.web.curd.query.QueryListRequest;
import cn.hamm.airpower.web.tree.TreeUtil;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("menu")
@Description("菜单")
public class MenuController extends BaseController<MenuEntity, MenuService, MenuRepository> {
    @Permission(authorize = false)
    @Override
    public Json getList(@RequestBody @NotNull QueryListRequest<MenuEntity> queryListRequest) {
        MenuEntity filter = queryListRequest.getFilter();
        queryListRequest.setSort(Objects.requireNonNullElse(
                queryListRequest.getSort(),
                new Sort().setField(MenuService.ORDER_FIELD_NAME)
        ));
        queryListRequest.setFilter(filter);
        return Json.data(TreeUtil.buildTreeList(service.getList(queryListRequest)));
    }
}
