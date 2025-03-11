package cn.hamm.spms.module.iot.parameter;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_DELETE;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("parameter")
@Description("采集参数")
public class ParameterController extends BaseController<ParameterEntity, ParameterService, ParameterRepository> {
    @Override
    protected ParameterEntity beforeAppUpdate(@NotNull ParameterEntity parameter) {
        FORBIDDEN_DELETE.when(parameter.getIsSystem(), "系统内置参数不允许编辑!");
        return parameter;
    }

    @Override
    protected void beforeAppDelete(long id) {
        ParameterEntity parameter = service.get(id);
        FORBIDDEN_DELETE.when(parameter.getIsSystem(), "系统内置参数不允许删除!");
    }
}
