package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("structure")
@Description("工厂结构")
public class StructureController extends BaseController<StructureEntity, StructureService, StructureRepository> {
}
