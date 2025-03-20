package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("material")
@Description("物料")
public class MaterialController extends BaseController<MaterialEntity, MaterialService, MaterialRepository> {
}
