package com.qqlab.spms.module.wms.move;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.wms.move.detail.MoveDetailEntity;
import com.qqlab.spms.module.wms.move.detail.MoveDetailRepository;
import com.qqlab.spms.module.wms.move.detail.MoveDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("move")
@Description("移库")
@Extends(exclude = Api.Delete)
public class MoveController extends BaseBillController<MoveEntity, MoveService, MoveRepository, MoveDetailEntity, MoveDetailService, MoveDetailRepository> {
}
