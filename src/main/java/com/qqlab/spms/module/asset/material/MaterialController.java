package com.qqlab.spms.module.asset.material;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.security.Permission;
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
@Permission(login = false)
public class MaterialController extends BaseController<MaterialService, MaterialVo> {
}
