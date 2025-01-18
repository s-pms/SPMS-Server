package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import cn.hamm.spms.module.system.config.ConfigFlag;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

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
    protected void afterDetailSaved(@NotNull SaleEntity sale) {
        // 计算总金额
        List<SaleDetailEntity> details = sale.getDetails();
        double totalPrice = details.stream()
                .mapToDouble(detail -> NumberUtil.mul(detail.getQuantity(), detail.getPrice()))
                .sum();
        sale.setTotalPrice(totalPrice);
        updateToDatabase(sale);
    }

    @Override
    protected ConfigFlag getAutoAuditConfigFlag() {
        return ConfigFlag.SALE_BILL_AUTO_AUDIT;
    }
}
