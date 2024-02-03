package com.qqlab.spms.module.channel.supplier;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.result.json.Json;
import com.qqlab.spms.base.BaseController;
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
@Extends(exclude = Api.Delete)
public class SupplierController extends BaseController<SupplierEntity, SupplierService, SupplierRepository> {
    @RequestMapping("test")
    public Json test() {
        return json("ok");
    }
}
