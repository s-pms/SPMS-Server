package cn.hamm.spms.module.wms.move;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.airpower.web.annotation.Extends;
import cn.hamm.airpower.web.enums.Api;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.wms.move.detail.MoveDetailEntity;
import cn.hamm.spms.module.wms.move.detail.MoveDetailRepository;
import cn.hamm.spms.module.wms.move.detail.MoveDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("move")
@Description("移库")
@Extends(exclude = Api.Delete)
public class MoveController extends BaseBillController<MoveEntity, MoveService, MoveRepository, MoveDetailEntity, MoveDetailService, MoveDetailRepository> {
}
