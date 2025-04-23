package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("structure")
@Description("生产单元")
public class StructureController extends BaseController<StructureEntity, StructureService, StructureRepository> {
}
