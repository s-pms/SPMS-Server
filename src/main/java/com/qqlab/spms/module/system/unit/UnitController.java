package com.qqlab.spms.module.system.unit;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.Permission;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("unit")
@Description("单位")
@Permission(login = false)
public class UnitController extends RootEntityController<UnitService, UnitEntity> {
}
