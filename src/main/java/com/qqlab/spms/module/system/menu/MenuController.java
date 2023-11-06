package com.qqlab.spms.module.system.menu;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.Permission;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("menu")
@Description("菜单")
public class MenuController extends RootEntityController<MenuService, MenuEntity> {
    @Permission(authorize = false)
    @Override
    public JsonData getList(@RequestBody QueryRequest<MenuEntity> queryRequest) {
        return super.getList(queryRequest);
    }
}
