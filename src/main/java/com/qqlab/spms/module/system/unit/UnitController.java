package com.qqlab.spms.module.system.unit;

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
@RequestMapping("unit")
@Description("单位")
public class UnitController extends BaseController<UnitEntity, UnitService, UnitRepository> {
}
