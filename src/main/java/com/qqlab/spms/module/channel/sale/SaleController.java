package com.qqlab.spms.module.channel.sale;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.helper.BillHelper;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailEntity;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailRepository;
import com.qqlab.spms.module.channel.sale.detail.SaleDetailService;
import com.qqlab.spms.module.wms.output.OutputEntity;
import com.qqlab.spms.module.wms.output.OutputStatus;
import com.qqlab.spms.module.wms.output.detail.OutputDetailEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("sale")
@Description("销售单")
public class SaleController extends BaseBillController<SaleEntity, SaleService, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {
    @Autowired
    private SaleDetailService saleDetailService;

    @Override
    public void afterAudit(SaleEntity bill) {
        OutputEntity outputBill = new OutputEntity()
                .setStatus(OutputStatus.AUDITING.getValue())
                .setSale(bill);
        List<SaleDetailEntity> details = saleDetailService.getAllByBillId(bill.getId());
        List<OutputDetailEntity> outputDetails = new ArrayList<>();
        for (SaleDetailEntity detail : details) {
            outputDetails.add(new OutputDetailEntity()
                    .setMaterial(detail.getMaterial())
                    .setQuantity(detail.getQuantity())
            );
        }
        outputBill.setDetails(outputDetails);
        BillHelper.addOutputBill(outputBill);
    }
}
