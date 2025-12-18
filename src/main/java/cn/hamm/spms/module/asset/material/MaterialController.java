package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.curd.Curd;
import cn.hamm.airpower.curd.query.QueryListRequest;
import cn.hamm.airpower.curd.query.QueryPageRequest;
import cn.hamm.airpower.meta.ExposeAll;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.system.unit.UnitEntity;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("material")
@Description("物料")
@Extends({Curd.Export, Curd.QueryExport})
public class MaterialController extends BaseController<MaterialEntity, MaterialService, MaterialRepository> {
    @Override
    @ExposeAll(MaterialEntity.class)
    public Json getPage(QueryPageRequest<MaterialEntity> queryPageRequest) {
        return super.getPage(queryPageRequest);
    }

    @Override
    @ExposeAll({UnitEntity.class})
    public Json getDetail(@NotNull MaterialEntity source) {
        return super.getDetail(source);
    }

    @Override
    @ExposeAll({MaterialEntity.class,})
    public Json getList(QueryListRequest<MaterialEntity> queryListRequest) {
        return super.getList(queryListRequest);
    }
}
