package cn.hamm.spms.module.mes.plan;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailRepository;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("plan")
@Description("生产计划")
@Extends(exclude = Api.Delete)
public class PlanController extends BaseBillController<PlanEntity, PlanService, PlanRepository, PlanDetailEntity, PlanDetailService, PlanDetailRepository> {
}
