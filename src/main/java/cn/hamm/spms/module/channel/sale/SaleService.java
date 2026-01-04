package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.core.NumberUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import cn.hamm.spms.module.channel.sale.enums.SaleStatus;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.enums.OutputStatus;
import cn.hamm.spms.module.wms.output.enums.OutputType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static cn.hamm.spms.module.system.config.enums.ConfigFlag.SALE_BILL_AUTO_AUDIT;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class SaleService extends AbstractBaseBillService<SaleEntity, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {
    @Override
    public IDictionary getRejectedStatus() {
        return SaleStatus.REJECTED;
    }

    @Override
    public IDictionary getAuditedStatus() {
        return SaleStatus.OUTPUTTING;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return SaleStatus.AUDITING;
    }

    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return SaleStatus.DONE;
    }

    @Override
    protected void afterDetailSaved(long billId) {
        // 计算总金额
        List<SaleDetailEntity> details = detailService.getAllByBillId(billId);
        double totalPrice = details.stream()
                .mapToDouble(detail -> NumberUtil.multiply(
                        detail.getQuantity(), detail.getPrice())
                )
                .sum();
        updateToDatabase(getEntityInstance(billId).setTotalPrice(totalPrice));
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return SALE_BILL_AUTO_AUDIT;
    }


    @Override
    protected void afterBillAudited(long billId) {
        SaleEntity sale = get(billId);
        // 销售单审核完毕后创建出库单
        OutputEntity outputBill = new OutputEntity()
                .setStatus(OutputStatus.AUDITING.getKey())
                .setSale(sale)
                .setType(OutputType.SALE.getKey());
        List<SaleDetailEntity> details = detailService.getAllByBillId(sale.getId());
        List<OutputDetailEntity> outputDetails = details.stream()
                .map(detail -> new OutputDetailEntity()
                        .setMaterial(detail.getMaterial())
                        .setQuantity(detail.getQuantity()))
                .collect(Collectors.toList());
        outputBill.setDetails(outputDetails);
        Services.getOutputService().add(outputBill);
    }
}
