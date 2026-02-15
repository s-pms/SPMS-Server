package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.curd.annotation.Extends;
import cn.hamm.spms.base.BaseController;

import static cn.hamm.airpower.curd.base.Curd.GetDetail;
import static cn.hamm.airpower.curd.base.Curd.GetPage;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("inventory")
@Description("库存")
@Extends({GetDetail, GetPage})
public class InventoryController extends BaseController<InventoryEntity, InventoryService, InventoryRepository> {
}
