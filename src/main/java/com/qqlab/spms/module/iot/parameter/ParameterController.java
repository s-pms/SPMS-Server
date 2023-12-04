package com.qqlab.spms.module.iot.parameter;

import cn.hamm.airpower.annotation.Description;
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
}