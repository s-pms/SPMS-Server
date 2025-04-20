package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.curd.Curd;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("input")
@Description("入库")
@Extends(exclude = Curd.Delete)
public class InputController extends BaseBillController<InputEntity, InputService, InputRepository, InputDetailEntity, InputDetailService, InputDetailRepository> {
}
