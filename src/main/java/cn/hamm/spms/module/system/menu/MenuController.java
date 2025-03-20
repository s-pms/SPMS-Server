package cn.hamm.spms.module.system.menu;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.model.Json;
import cn.hamm.airpower.core.model.tree.TreeUtil;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.airpower.web.annotation.Permission;
import cn.hamm.airpower.web.model.query.QueryListRequest;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("menu")
@Description("菜单")
public class MenuController extends BaseController<MenuEntity, MenuService, MenuRepository> {
    @Permission(authorize = false)
    @Override
    public Json getList(@RequestBody QueryListRequest<MenuEntity> queryListRequest) {
        return Json.data(TreeUtil.buildTreeList(service.getList(queryListRequest)));
    }
}
