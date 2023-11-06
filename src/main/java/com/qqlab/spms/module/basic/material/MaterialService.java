package com.qqlab.spms.module.basic.material;

import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.module.basic.unit.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return entity;
    }
}
