package com.qqlab.spms.module.mes.plan;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.mes.plan.detail.PlanDetailEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("plan")
@Description("生产计划")
@Extends(exclude = Api.Delete)
public class PlanController extends BaseBillController<PlanEntity, PlanService, PlanRepository, PlanDetailEntity, PlanDetailService, PlanDetailRepository> {
}
