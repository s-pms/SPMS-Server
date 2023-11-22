package com.qqlab.spms.module.channel.purchase;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailRepository;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("purchase")
@Description("采购单")
public class PurchaseController extends BaseBillController<PurchaseEntity, PurchaseService, PurchaseRepository, PurchaseDetailEntity, PurchaseDetailService, PurchaseDetailRepository> {

}
