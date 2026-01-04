package cn.hamm.spms.module.channel.supplier;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.airpower.web.curd.Curd;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("supplier")
@Description("供应商")
@Extends(exclude = Curd.Delete)
public class SupplierController extends BaseController<SupplierEntity, SupplierService, SupplierRepository> {

}
