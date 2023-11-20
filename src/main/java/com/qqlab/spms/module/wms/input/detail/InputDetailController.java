package com.qqlab.spms.module.wms.input.detail;

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
@RequestMapping("inputDetail")
@Description("入库明细")
@Extends(exclude = Api.Delete)
public class InputDetailController extends BaseBillDetailController<InputDetailEntity, InputDetailService, InputDetailRepository> {
}
