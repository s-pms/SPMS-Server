package cn.hamm.spms.module.mes.plan;

import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailRepository;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class PlanService extends AbstractBaseBillService<PlanEntity, PlanRepository, PlanDetailEntity, PlanDetailService, PlanDetailRepository> {
    @Override
    public IDictionary getAuditedStatus() {
        return PlanStatus.PRODUCING;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return PlanStatus.AUDITING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return PlanStatus.REJECTED;
    }
}