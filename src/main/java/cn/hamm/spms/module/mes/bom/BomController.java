package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
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
