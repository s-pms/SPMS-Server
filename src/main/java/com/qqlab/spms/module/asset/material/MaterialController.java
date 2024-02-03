package com.qqlab.spms.module.asset.material;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("material")
@Description("物料")
public class MaterialController extends BaseController<MaterialEntity, MaterialService, MaterialRepository> {
}
