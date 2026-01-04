package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.airpower.web.curd.Curd;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailRepository;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("output")
@Description("出库")
@Extends(exclude = Curd.Delete)
public class OutputController extends BaseBillController<OutputEntity, OutputService, OutputRepository, OutputDetailEntity, OutputDetailService, OutputDetailRepository> {
}
