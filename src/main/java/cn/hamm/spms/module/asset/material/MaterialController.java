package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.airpower.web.curd.Curd;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("material")
@Description("物料")
@Extends({Curd.Export, Curd.QueryExport})
public class MaterialController extends BaseController<MaterialEntity, MaterialService, MaterialRepository> {
}
