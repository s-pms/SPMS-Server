package cn.hamm.spms.module.mes.operation;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author zfy
 */
@RestController
@Description("工序")
@Api("operation")
public class OperationController extends BaseController<OperationEntity, OperationService, OperationRepository> {

}
