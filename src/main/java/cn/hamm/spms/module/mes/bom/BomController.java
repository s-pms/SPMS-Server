package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.bill.BaseBillController;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import cn.hamm.spms.module.mes.bom.detail.BomDetailRepository;
import cn.hamm.spms.module.mes.bom.detail.BomDetailService;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("bom")
@Description("BOM")
public class BomController extends BaseBillController<BomEntity, BomService, BomRepository, BomDetailEntity, BomDetailService, BomDetailRepository> {
}
