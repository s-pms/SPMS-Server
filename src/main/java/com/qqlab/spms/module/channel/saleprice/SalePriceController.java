package com.qqlab.spms.module.channel.saleprice;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("salePrice")
@Description("销售价格")
public class SalePriceController extends BaseController<SalePriceService, SalePriceEntity> {
}
