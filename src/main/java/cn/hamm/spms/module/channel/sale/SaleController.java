package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.OutputStatus;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("sale")
@Description("销售单")
public class SaleController extends BaseBillController<SaleEntity, SaleService, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {
    @Autowired
    private SaleDetailService saleDetailService;

    @Override
    public void afterAudit(@NotNull SaleEntity bill) {
        OutputEntity outputBill = new OutputEntity()
                .setStatus(OutputStatus.AUDITING.getKey())
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
        Services.getOutputService().add(outputBill);
    }
}
