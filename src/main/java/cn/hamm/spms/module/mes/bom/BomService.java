package cn.hamm.spms.module.mes.bom;

import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import cn.hamm.spms.module.mes.bom.detail.BomDetailRepository;
import cn.hamm.spms.module.mes.bom.detail.BomDetailService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class BomService extends AbstractBaseBillService<BomEntity, BomRepository, BomDetailEntity, BomDetailService, BomDetailRepository> {

    @Override
    public BomEntity setAudited(BomEntity bill) {
        return bill;
    }

    @Override
    public BomEntity setAuditing(BomEntity bill) {
        return bill;
    }

    @Override
    public boolean isAudited(BomEntity bill) {
        return false;
    }

    @Override
    public boolean canReject(BomEntity bill) {
        return false;
    }

    @Override
    public BomEntity setReject(BomEntity bill) {
        return bill;
    }

    @Override
    public boolean canEdit(BomEntity bill) {
        return false;
    }
}
