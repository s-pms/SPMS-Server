package com.qqlab.spms.module.channel.saleprice;

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
public class SalePriceService extends BaseService<SalePriceEntity, SalePriceRepository> {
    @Override
    protected SalePriceEntity beforeAdd(SalePriceEntity entity) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(entity.getCustomer(), entity.getMaterial());
        if (Objects.nonNull(exist)) {
            Result.FORBIDDEN_EXIST.show(exist.getCustomer().getName() + "-" + exist.getMaterial().getName() + " 销售价已存在!");
        }
        return entity;
    }
}
