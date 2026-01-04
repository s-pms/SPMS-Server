package cn.hamm.spms.module.system.status;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.access.Permission;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.ApiController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Permission(login = false)
@Api("status")
@Description("系统状态")
public class StatusController extends ApiController {
}
