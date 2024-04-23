package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
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
    protected void afterDetailSaved(SaleEntity sale) {
        List<SaleDetailEntity> details = sale.getDetails();
        double totalPrice = 0D;
        for (SaleDetailEntity detail : details) {
            totalPrice += detail.getQuantity() * detail.getPrice();
        }
        sale.setTotalPrice(totalPrice);
        updateToDatabase(sale);
    }
}
