package cn.hamm.spms.module.system.status;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.root.RootController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Permission(login = false)
@ApiController("status")
@Description("系统状态")
public class StatusController extends RootController {
}
