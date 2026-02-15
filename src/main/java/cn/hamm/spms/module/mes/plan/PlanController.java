package cn.hamm.spms.module.mes.plan;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.curd.annotation.Extends;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailRepository;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailService;

import static cn.hamm.airpower.curd.base.Curd.Delete;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("plan")
@Description("生产计划")
@Extends(exclude = Delete)
public class PlanController extends BaseBillController<PlanEntity, PlanService, PlanRepository, PlanDetailEntity, PlanDetailService, PlanDetailRepository> {
}
