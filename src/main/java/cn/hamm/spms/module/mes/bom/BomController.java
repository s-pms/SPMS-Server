package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.ExposeAll;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("bom")
@Description("BOM")
public class BomController extends BaseController<BomEntity, BomService, BomRepository> {
    @Override
    @ExposeAll({BomEntity.class, BomDetailEntity.class, MaterialEntity.class})
    public Json getDetail(@NotNull BomEntity source) {
        return super.getDetail(source);
    }
}
