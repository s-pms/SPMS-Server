package com.qqlab.spms.module.mes.plan;

import com.qqlab.spms.base.bill.BaseBillRepository;
import com.qqlab.spms.module.mes.plan.detail.PlanDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface PlanRepository extends BaseBillRepository<PlanEntity, PlanDetailEntity> {
}
