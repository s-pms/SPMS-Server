package cn.hamm.spms.module.channel.sale;

import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailRepository;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class SaleService extends AbstractBaseBillService<SaleEntity, SaleRepository, SaleDetailEntity, SaleDetailService, SaleDetailRepository> {
    @Override
    public SaleEntity setAudited(SaleEntity bill) {
        return bill.setStatus(SaleStatus.OUTPUTTING.getKey());
    }

    @Override
    public SaleEntity setAuditing(SaleEntity bill) {
        return bill.setStatus(SaleStatus.AUDITING.getKey());
    }

    @Override
    public boolean isAudited(SaleEntity bill) {
        return bill.getStatus() != SaleStatus.AUDITING.getKey();
    }

    @Override
    public boolean canReject(SaleEntity bill) {
        return bill.getStatus() == SaleStatus.AUDITING.getKey();
    }

    @Override
    public SaleEntity setReject(SaleEntity bill) {
        return bill.setStatus(SaleStatus.REJECTED.getKey());
    }

    @Override
    public boolean canEdit(SaleEntity bill) {
        return bill.getStatus() == SaleStatus.REJECTED.getKey();
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
