package com.qqlab.spms.module.wms.input;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.wms.input.detail.InputDetailEntity;
import com.qqlab.spms.module.wms.input.detail.InputDetailRepository;
import com.qqlab.spms.module.wms.input.detail.InputDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("input")
@Description("入库")
@Extends(exclude = Api.Delete)
public class InputController extends BaseBillController<InputEntity, InputService, InputRepository, InputDetailEntity, InputDetailService, InputDetailRepository> {
}
