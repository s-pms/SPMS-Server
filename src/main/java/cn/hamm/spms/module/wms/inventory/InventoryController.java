package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.common.annotation.LogDisabled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("inventory")
@Description("入库")
@Extends({Api.GetDetail, Api.GetList})
public class InventoryController extends BaseController<InventoryEntity, InventoryService, InventoryRepository> {
    @LogDisabled
    @Override
    public JsonData getList(@RequestBody QueryRequest<InventoryEntity> queryRequest) {
        List<InventoryEntity> list;
        InventoryEntity filter = queryRequest.getFilter();
        if (Objects.nonNull(filter)) {
            if (filter.getType() == InventoryType.STORAGE.getKey()) {
                list = service.getListByStorage(filter.getStorage());
                return jsonData(list);
            }
            if (filter.getType() == InventoryType.STRUCTURE.getKey()) {
                list = service.getListByStructure(filter.getStructure());
                return jsonData(list);
            }
        }
        list = service.getList(queryRequest);
        return jsonData(list);
    }
}
