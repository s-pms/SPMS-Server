package com.qqlab.spms.module.basic.supplier;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.Permission;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("supplier")
@Description("供应商")
@Permission(login = false)
@Extends(exclude = Api.Delete)
public class SupplierController extends RootEntityController<SupplierService, SupplierEntity> {
}
