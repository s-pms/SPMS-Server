package com.qqlab.spms.module.channel.sale;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseController;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SaleController extends BaseController<SaleService, SaleEntity> {
    @Autowired
    private SaleDetailService detailService;

    @Override
    protected SaleEntity afterGetDetail(SaleEntity entity) {
        return entity.setDetails(detailService.getAllByBillId(entity.getId()));
    }
}
