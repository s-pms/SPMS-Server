package com.qqlab.spms.module.channel.purchase;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseController;
import com.qqlab.spms.module.channel.purchase.detail.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PurchaseController extends BaseController<PurchaseService, PurchaseEntity> {
    @Autowired
    private PurchaseDetailService detailService;

    @Override
    protected PurchaseEntity afterGetDetail(PurchaseEntity entity) {
        return entity.setDetails(detailService.getAllByBillId(entity.getId()));
    }
}
