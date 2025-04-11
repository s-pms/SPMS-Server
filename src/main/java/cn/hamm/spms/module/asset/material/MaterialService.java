package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.mcp.method.McpMethod;
import cn.hamm.airpower.mcp.method.McpOptional;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.unit.UnitService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class MaterialService extends BaseService<MaterialEntity, MaterialRepository> {
    private final UnitService unitService;

    public MaterialService(UnitService unitService) {
        super();
        this.unitService = unitService;
    }

    @Override
    protected MaterialEntity beforeAppSaveToDatabase(@NotNull MaterialEntity material) {
        material.setPurchasePrice(Objects.requireNonNullElse(material.getPurchasePrice(), 0D));
        material.setSalePrice(Objects.requireNonNullElse(material.getSalePrice(), 0D));
        return material;
    }

    @McpMethod()
    @Description("add new material by name and spc")
    public String addMaterial(
            @Description("material name/product name")
            String name,

            @McpOptional
            @Description("material spc, not required, set '我不知道' if blank")
            String spc) {
        MaterialEntity material = new MaterialEntity().setName(name).setSpc(spc).setMaterialType(MaterialType.PURCHASE.getKey()).setUnitInfo(unitService.get(1L)).setUseType(MaterialUseType.CONSUMABLE.getKey());
        add(material);
        return name + "已经成功添加到物料库";
    }
}
