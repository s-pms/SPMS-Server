package com.qqlab.spms.module.asset.material;

import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class MaterialService extends BaseService<MaterialEntity, MaterialRepository> {

    @Override
    protected MaterialEntity beforeSaveToDatabase(MaterialEntity entity) {
        if(Objects.isNull(entity.getPurchasePrice())){
            entity.setPurchasePrice(0D);
        }
        if(Objects.isNull(entity.getSalePrice())){
            entity.setSalePrice(0D);
        }
        return super.beforeSaveToDatabase(entity);
    }
}
