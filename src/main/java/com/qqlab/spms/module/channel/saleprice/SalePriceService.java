package com.qqlab.spms.module.channel.saleprice;

import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.channel.customer.CustomerEntity;
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
    public SalePriceEntity add(SalePriceEntity entity) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(entity.getCustomer(), entity.getMaterial());
        if (Objects.nonNull(exist)) {
            Result.FORBIDDEN_EXIST.show(exist.getCustomer().getName() + "-" + exist.getMaterial().getName() + " 销售价已存在!");
        }
        return addToDatabase(entity);
    }


    @Override
    public SalePriceEntity update(SalePriceEntity entity) {
        entity.setMaterial(null).setCustomer(null);
        return updateToDatabase(entity);
    }

    protected SalePriceEntity getByMaterialAndCustomer(MaterialEntity materialEntity, CustomerEntity customerEntity) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(customerEntity, materialEntity);
        Result.DATA_NOT_FOUND.whenNull(exist, "没有查询到该物料在此客户下提供的销售价格，请参考物料标准销售价填写。");
        return exist;
    }
}
