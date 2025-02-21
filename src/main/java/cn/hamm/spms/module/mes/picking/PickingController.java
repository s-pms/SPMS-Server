package cn.hamm.spms.module.mes.picking;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailEntity;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailRepository;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("picking")
@Description("领料单")
public class PickingController extends BaseBillController<
        PickingEntity, PickingService, PickingRepository,
        PickingDetailEntity, PickingDetailService, PickingDetailRepository> {

}
