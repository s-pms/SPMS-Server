package cn.hamm.spms.module.system.log;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("log")
@Description("日志")
public class LogController extends BaseController<LogEntity, LogService, LogRepository> {
}
