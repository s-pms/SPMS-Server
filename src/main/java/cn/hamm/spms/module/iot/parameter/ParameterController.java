package cn.hamm.spms.module.iot.parameter;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_DELETE;

/**
 * <h1>采集参数</h1>
 *
 * @author Hamm.cn
 */
@Api("parameter")
@Description("采集参数")
public class ParameterController extends BaseController<ParameterEntity, ParameterService, ParameterRepository> {
    /**
     * 拦截内置参数被修改
     *
     * @param parameter 本次提交的参数
     * @param exist     数据库中的原值
     * @return 放行的参数
     * @apiNote {@code isSystem} 取自库中原值而非提交值，前端只读标记绕不过这一层
     */
    @Override
    protected ParameterEntity beforeAppUpdate(@NotNull ParameterEntity parameter, @NotNull ParameterEntity exist) {
        FORBIDDEN_DELETE.when(exist.getIsSystem(), "系统内置参数不允许编辑!");
        return parameter;
    }

    /**
     * 拦截内置参数被删除
     *
     * @param parameter 待删除的参数
     */
    @Override
    protected void beforeAppDelete(@NotNull ParameterEntity parameter) {
        FORBIDDEN_DELETE.when(parameter.getIsSystem(), "系统内置参数不允许删除!");
    }
}
