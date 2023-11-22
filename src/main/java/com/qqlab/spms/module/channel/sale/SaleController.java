package com.qqlab.spms.module.channel.sale;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailEntity;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailRepository;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("sale")
@Description("销售单")
public class SaleController extends BaseBillController<SaleEntity, SaleService, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {

}
