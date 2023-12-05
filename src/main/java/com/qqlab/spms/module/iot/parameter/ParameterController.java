package com.qqlab.spms.module.iot.parameter;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("parameter")
@Description("采集参数")
public class ParameterController extends BaseController<ParameterEntity, ParameterService, ParameterRepository> {
    @Override
    protected ParameterEntity beforeUpdate(ParameterEntity entity) {
        Result.FORBIDDEN_DELETE.when(entity.getIsSystem(), "系统内置参数不允许编辑!");
        return entity;
    }

    @Override
    protected ParameterEntity beforeDelete(ParameterEntity entity) {
        Result.FORBIDDEN_DELETE.when(entity.getIsSystem(), "系统内置参数不允许删除!");
        return entity;
    }
}
