package cn.hamm.spms.module.iot.parameter;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("parameter")
@Description("采集参数")
public class ParameterController extends BaseController<ParameterEntity, ParameterService, ParameterRepository> {
    @Override
    protected ParameterEntity beforeUpdate(@NotNull ParameterEntity entity) {
        ServiceError.FORBIDDEN_DELETE.when(entity.getIsSystem(), "系统内置参数不允许编辑!");
        return entity;
    }

    @Override
    protected void beforeDelete(long id) {
        ParameterEntity parameter = service.get(id);
        ServiceError.FORBIDDEN_DELETE.when(parameter.getIsSystem(), "系统内置参数不允许删除!");
    }
}
