package cn.hamm.spms.module.system.log;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("log")
@Description("日志")
public class LogController extends BaseController<LogEntity, LogService, LogRepository> {
}
