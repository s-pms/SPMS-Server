package cn.hamm.spms.module.channel.supplier;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.airpower.web.annotation.Extends;
import cn.hamm.airpower.web.enums.Api;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("supplier")
@Description("供应商")
@Extends(exclude = Api.Delete)
public class SupplierController extends BaseController<SupplierEntity, SupplierService, SupplierRepository> {

}
