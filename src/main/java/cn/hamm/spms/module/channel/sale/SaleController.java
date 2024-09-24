package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.OutputService;
import cn.hamm.spms.module.wms.output.OutputStatus;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("sale")
@Description("销售单")
public class SaleController extends BaseBillController<SaleEntity, SaleService, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {
    @Autowired
    private OutputService outputService;

    @Override
    public void afterAudit(@NotNull SaleEntity bill) {
        OutputEntity outputBill = new OutputEntity()
                .setStatus(OutputStatus.AUDITING.getKey())
                .setSale(bill);
        List<SaleDetailEntity> details = detailService.getAllByBillId(bill.getId());
        List<OutputDetailEntity> outputDetails = details.stream()
                .map(detail -> new OutputDetailEntity()
                        .setMaterial(detail.getMaterial())
                        .setQuantity(detail.getQuantity()))
                .collect(Collectors.toList());
        outputBill.setDetails(outputDetails);
        outputService.add(outputBill);
    }
}
