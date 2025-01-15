package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("bom")
@Description("BOM")
public class BomController extends BaseController<BomEntity, BomService, BomRepository> {
}
