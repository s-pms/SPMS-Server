package cn.hamm.spms.module.channel.purchase;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailEntity;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailRepository;
import cn.hamm.spms.module.channel.purchase.detail.PurchaseDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("purchase")
@Description("采购单")
public class PurchaseController extends BaseBillController<
        PurchaseEntity, PurchaseService, PurchaseRepository,
        PurchaseDetailEntity, PurchaseDetailService, PurchaseDetailRepository
        > {

}
