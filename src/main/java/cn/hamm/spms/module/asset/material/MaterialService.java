package cn.hamm.spms.module.asset.material;

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
        if (Objects.isNull(material.getPurchasePrice())) {
            material.setPurchasePrice(0D);
        }
        if (Objects.isNull(material.getSalePrice())) {
            material.setSalePrice(0D);
        }
        return material;
    }
}
