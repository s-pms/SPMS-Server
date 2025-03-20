package cn.hamm.spms.module.mes.plan;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.airpower.web.annotation.Extends;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailRepository;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailService;

import static cn.hamm.airpower.web.enums.Api.Delete;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("plan")
@Description("生产计划")
@Extends(exclude = Delete)
public class PlanController extends BaseBillController<PlanEntity, PlanService, PlanRepository, PlanDetailEntity, PlanDetailService, PlanDetailRepository> {
}
