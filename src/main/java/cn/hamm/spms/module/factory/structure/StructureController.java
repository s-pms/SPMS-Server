package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("structure")
@Description("工厂结构")
public class StructureController extends BaseController<StructureEntity, StructureService, StructureRepository> {
}
