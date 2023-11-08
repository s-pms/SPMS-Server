package com.qqlab.spms.module.asset.material;

import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.module.system.unit.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class MaterialService extends BaseService<MaterialEntity, MaterialRepository> {
    @Autowired
    private UnitService unitService;

    @Override
    protected MaterialEntity beforeSaveToDatabase(MaterialEntity entity) {
        entity.setUnitInfo(unitService.getById(entity.getUnitInfo().getId()));
        if(Objects.isNull(entity.getPurchasePrice())){
            entity.setPurchasePrice(0D);
        }
        if(Objects.isNull(entity.getSalePrice())){
            entity.setSalePrice(0D);
        }
        return super.beforeSaveToDatabase(entity);
    }
}
