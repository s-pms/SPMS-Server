package com.qqlab.spms.module.wms.output.detail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import com.qqlab.spms.base.bill.detail.BaseBillDetailController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("outputDetail")
@Description("出库明细")
@Extends(exclude = Api.Delete)
public class OutputDetailController extends BaseBillDetailController<OutputDetailEntity, OutputDetailService, OutputDetailRepository> {
}
