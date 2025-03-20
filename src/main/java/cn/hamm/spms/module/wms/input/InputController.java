package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.airpower.web.annotation.Extends;
import cn.hamm.airpower.web.enums.Api;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("input")
@Description("入库")
@Extends(exclude = Api.Delete)
public class InputController extends BaseBillController<InputEntity, InputService, InputRepository, InputDetailEntity, InputDetailService, InputDetailRepository> {
}
