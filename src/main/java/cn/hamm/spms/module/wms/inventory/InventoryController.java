package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.spms.base.BaseController;

import static cn.hamm.airpower.web.curd.Curd.GetDetail;
import static cn.hamm.airpower.web.curd.Curd.GetPage;

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
