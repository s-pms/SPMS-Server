package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.enums.Api;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailRepository;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("output")
@Description("出库")
@Extends(exclude = Api.Delete)
public class OutputController extends BaseBillController<OutputEntity, OutputService, OutputRepository, OutputDetailEntity, OutputDetailService, OutputDetailRepository> {
}
