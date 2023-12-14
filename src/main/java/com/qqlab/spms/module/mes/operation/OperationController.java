package com.qqlab.spms.module.mes.operation;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author zfy
 * @date 2023/12/14
 */
@RestController
@Description("工序")
@RequestMapping("operation")
public class OperationController extends BaseController<OperationEntity, OperationService, OperationRepository> {

}
