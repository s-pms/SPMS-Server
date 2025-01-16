package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("config")
@Description("系统配置")
public class ConfigController extends BaseController<ConfigEntity, ConfigService, ConfigRepository> {
}
