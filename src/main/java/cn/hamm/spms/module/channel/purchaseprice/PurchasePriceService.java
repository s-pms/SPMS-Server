package cn.hamm.spms.module.channel.purchaseprice;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.supplier.SupplierEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.DATA_NOT_FOUND;
import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_EXIST;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class PurchasePriceService extends BaseService<PurchasePriceEntity, PurchasePriceRepository> {
    @Override
    protected @NotNull PurchasePriceEntity beforeAdd(@NotNull PurchasePriceEntity purchasePrice) {
        PurchasePriceEntity exist = repository.getBySupplierAndMaterial(
                purchasePrice.getSupplier(),
                purchasePrice.getMaterial()
        );
        if (Objects.nonNull(exist)) {
            FORBIDDEN_EXIST.show(String.format(
                    "物料 %s 在供应商 %s 下的采购价已存在!",
                    exist.getMaterial().getName(),
                    exist.getSupplier().getName()
            ));
        }
        return purchasePrice;
    }

    protected PurchasePriceEntity getByMaterialAndSupplier(MaterialEntity material, SupplierEntity supplier) {
        PurchasePriceEntity exist = repository.getBySupplierAndMaterial(supplier, material);
        DATA_NOT_FOUND.whenNull(exist, "没有查询到该物料在此供应商下提供的采购价格，请参考物料标准采购价填写。");
        return exist;
    }
}
