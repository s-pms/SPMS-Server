package cn.hamm.spms.module.mes.plan.detail;

import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>生产计划明细</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PlanDetailRepository extends BaseBillDetailRepository<PlanDetailEntity> {
}
