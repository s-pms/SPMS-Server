package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.DATA_NOT_FOUND;
import static cn.hamm.airpower.exception.Errors.FORBIDDEN_EXIST;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class SalePriceService extends BaseService<SalePriceEntity, SalePriceRepository> {
    @Override
    protected @NotNull SalePriceEntity beforeAdd(@NotNull SalePriceEntity salePrice) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(
                salePrice.getCustomer(),
                salePrice.getMaterial()
        );
        if (Objects.nonNull(exist)) {
            FORBIDDEN_EXIST.show(String.format(
                    "%s 在客户 %s 的销售价已经存在!",
                    exist.getMaterial().getName(),
                    exist.getCustomer().getName()
            ));
        }
        return salePrice;
    }

    @Override
    protected @NotNull SalePriceEntity beforeUpdate(@NotNull SalePriceEntity salePrice) {
        return salePrice.setMaterial(null).setCustomer(null);
    }

    protected SalePriceEntity getByMaterialAndCustomer(MaterialEntity material, CustomerEntity customer) {
        SalePriceEntity exist = repository.getByCustomerAndMaterial(customer, material);
        DATA_NOT_FOUND.whenNull(exist, "没有查询到该物料在此客户下提供的销售价格，请参考物料标准销售价填写。");
        return exist;
    }
}
