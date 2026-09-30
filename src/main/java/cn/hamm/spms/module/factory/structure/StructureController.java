package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>生产单元</h1>
 *
 * @author Hamm.cn
 */
@Api("structure")
@Description("生产单元")
public class StructureController extends BaseController<StructureEntity, StructureService, StructureRepository> {
}
