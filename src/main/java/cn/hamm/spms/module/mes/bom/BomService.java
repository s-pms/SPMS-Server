package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.interfaces.IDictionary;
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
    public IDictionary getAuditedStatus() {
        return BomStatus.PUBLISHED;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return BomStatus.AUDITING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return BomStatus.REJECTED;
    }
}
