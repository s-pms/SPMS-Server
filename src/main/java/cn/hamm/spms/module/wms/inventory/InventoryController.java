package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.enums.Api;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("inventory")
@Description("入库")
@Extends({Api.GetDetail, Api.GetPage})
public class InventoryController extends BaseController<InventoryEntity, InventoryService, InventoryRepository> {
}
