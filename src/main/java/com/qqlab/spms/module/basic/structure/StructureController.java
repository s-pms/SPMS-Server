package com.qqlab.spms.module.basic.structure;

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
@RequestMapping("structure")
@Description("工厂结构")
@Permission(login = false)
public class StructureController extends RootEntityController<StructureService, StructureEntity> {
}
