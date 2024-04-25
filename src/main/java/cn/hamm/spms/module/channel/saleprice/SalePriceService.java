package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.airpower.enums.Result;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class SalePriceService extends BaseService<SalePriceEntity, SalePriceRepository> {
    @Override
    protected @NotNull SalePriceEntity beforeAdd(@NotNull SalePriceEntity source) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(source.getCustomer(), source.getMaterial());
        if (Objects.nonNull(exist)) {
            Result.FORBIDDEN_EXIST.show(exist.getCustomer().getName() + "-" + exist.getMaterial().getName() + " 销售价已存在!");
        }
        return source;
    }

    @Override
    protected @NotNull SalePriceEntity beforeUpdate(@NotNull SalePriceEntity source) {
        return source.setMaterial(null).setCustomer(null);
    }

    protected SalePriceEntity getByMaterialAndCustomer(MaterialEntity materialEntity, CustomerEntity customerEntity) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(customerEntity, materialEntity);
        Result.DATA_NOT_FOUND.whenNull(exist, "没有查询到该物料在此客户下提供的销售价格，请参考物料标准销售价填写。");
        return exist;
    }
}
