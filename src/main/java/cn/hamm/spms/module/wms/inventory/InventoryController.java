package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.spms.base.BaseController;

import static cn.hamm.airpower.enums.Api.GetDetail;
import static cn.hamm.airpower.enums.Api.GetPage;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("inventory")
@Description("入库")
@Extends({GetDetail, GetPage})
public class InventoryController extends BaseController<InventoryEntity, InventoryService, InventoryRepository> {
}
