package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("config")
@Description("系统配置")
public class ConfigController extends BaseController<ConfigEntity, ConfigService, ConfigRepository> {
}
