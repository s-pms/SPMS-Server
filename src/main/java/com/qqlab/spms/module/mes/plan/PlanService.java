package com.qqlab.spms.module.mes.plan;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.module.mes.plan.detail.PlanDetailEntity;
import com.qqlab.spms.module.mes.plan.detail.PlanDetailRepository;
import com.qqlab.spms.module.mes.plan.detail.PlanDetailService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class PlanService extends AbstractBaseBillService<PlanEntity, PlanRepository, PlanDetailEntity, PlanDetailService, PlanDetailRepository> {
    @Override
    public PlanEntity setAudited(PlanEntity bill) {
        return bill.setStatus(PlanStatus.PRODUCING.getKey());
    }

    @Override
    public PlanEntity setAuditing(PlanEntity bill) {
        return bill.setStatus(PlanStatus.AUDITING.getKey());
    }

    @Override
    public boolean isAudited(PlanEntity bill) {
        return bill.getStatus() != PlanStatus.AUDITING.getKey();
    }

    @Override
    public boolean canReject(PlanEntity bill) {
        return bill.getStatus() == PlanStatus.AUDITING.getKey();
    }

    @Override
    public PlanEntity setReject(PlanEntity bill) {
        return bill.setStatus(PlanStatus.REJECTED.getKey());
    }

    @Override
    public boolean canEdit(PlanEntity bill) {
        return bill.getStatus() == PlanStatus.REJECTED.getKey();
    }
}