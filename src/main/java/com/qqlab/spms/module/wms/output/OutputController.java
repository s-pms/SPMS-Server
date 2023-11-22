package com.qqlab.spms.module.wms.output;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.wms.output.detail.OutputDetailEntity;
import com.qqlab.spms.module.wms.output.detail.OutputDetailRepository;
import com.qqlab.spms.module.wms.output.detail.OutputDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("output")
@Description("出库")
@Extends(exclude = Api.Delete)
public class OutputController extends BaseBillController<OutputEntity, OutputService, OutputRepository, OutputDetailEntity, OutputDetailService, OutputDetailRepository> {
}
