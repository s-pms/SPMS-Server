package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.model.query.QueryRequest;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.common.annotation.DisableLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("inventory")
@Description("入库")
@Extends({Api.GetDetail, Api.GetList})
public class InventoryController extends BaseController<InventoryEntity, InventoryService, InventoryRepository> {
    @DisableLog
    @Override
    public Json getList(@RequestBody @NotNull QueryRequest<InventoryEntity> queryRequest) {
        List<InventoryEntity> list;
        InventoryEntity filter = queryRequest.getFilter();
        if (Objects.nonNull(filter)) {
            if (filter.getType() == InventoryType.STORAGE.getKey()) {
                list = service.getListByStorage(filter.getStorage());
                return Json.data(list);
            }
            if (filter.getType() == InventoryType.STRUCTURE.getKey()) {
                list = service.getListByStructure(filter.getStructure());
                return Json.data(list);
            }
        }
        list = service.getList(queryRequest);
        return Json.data(list);
    }
}
