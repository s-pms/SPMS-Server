package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.airpower.exception.ServiceError;
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
            ServiceError.FORBIDDEN_EXIST.show(String.format(
                    "%s 在客户 %s 的销售价已经存在!",
                    exist.getMaterial().getName(),
                    exist.getCustomer().getName()
            ));
        }
        return source;
    }

    @Override
    protected @NotNull SalePriceEntity beforeUpdate(@NotNull SalePriceEntity source) {
        return source.setMaterial(null).setCustomer(null);
    }

    protected SalePriceEntity getByMaterialAndCustomer(MaterialEntity materialEntity, CustomerEntity customerEntity) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(customerEntity, materialEntity);
        ServiceError.DATA_NOT_FOUND.whenNull(exist, "没有查询到该物料在此客户下提供的销售价格，请参考物料标准销售价填写。");
        return exist;
    }
}
