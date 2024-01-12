package com.qqlab.spms.module.mes.craft;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author zfy
 */
@RestController
@Description("工艺路线")
@RequestMapping("craftRouter")
public class CraftRouterController extends BaseController<CraftRouterEntity, CraftRouterService, CraftRouterRepository> {

}
