package com.qqlab.spms.module.basic.purchaseprice;

import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class PurchasePriceService extends BaseService<PurchasePriceEntity, PurchasePriceRepository> {
    @Override
    protected PurchasePriceEntity beforeAdd(PurchasePriceEntity entity) {
        PurchasePriceEntity exist = repository.getBySupplierAndMaterial(entity.getSupplier(), entity.getMaterial());
        if (Objects.nonNull(exist)) {
            Result.FORBIDDEN_EXIST.show(exist.getSupplier().getName() + "-" + exist.getMaterial().getName() + " 采购价已存在!");
        }
        return entity;
    }
}
