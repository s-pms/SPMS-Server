package com.qqlab.spms.module.system.permission;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootEntityController;
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
public class PermissionController extends RootEntityController<PermissionService, PermissionEntity> {
}
