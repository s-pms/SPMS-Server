package cn.hamm.spms.module.mes.picking;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailEntity;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailRepository;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("picking")
@Description("领料单")
public class PickingController extends BaseBillController<
        PickingEntity, PickingService, PickingRepository,
        PickingDetailEntity, PickingDetailService, PickingDetailRepository> {

}
