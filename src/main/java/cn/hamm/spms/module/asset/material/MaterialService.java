package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.config.Constant;
import cn.hamm.spms.base.BaseService;
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
    @Override
    protected MaterialEntity beforeAppSaveToDatabase(@NotNull MaterialEntity material) {
        material.setPurchasePrice(Objects.requireNonNullElse(material.getPurchasePrice(), Constant.ZERO_DOUBLE));
        material.setSalePrice(Objects.requireNonNullElse(material.getSalePrice(), Constant.ZERO_DOUBLE));
        return material;
    }
}
