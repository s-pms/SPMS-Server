package com.qqlab.spms.module.system.permission;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("permission")
@Description("权限")
public class PermissionController extends BaseController<PermissionEntity, PermissionService, PermissionRepository> {
    @Override
    protected PermissionEntity beforeAdd(PermissionEntity entity) {
        return entity.setIsSystem(null);
    }

    @Override
    protected PermissionEntity beforeUpdate(PermissionEntity entity) {
        return entity.setIsSystem(null);
    }
}
