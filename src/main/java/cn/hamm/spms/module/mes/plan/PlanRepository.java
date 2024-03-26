package cn.hamm.spms.module.mes.plan;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface PlanRepository extends BaseBillRepository<PlanEntity, PlanDetailEntity> {
}