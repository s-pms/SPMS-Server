package cn.hamm.spms.module.channel.purchaseprice;

import cn.hamm.airpower.result.Result;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.supplier.SupplierEntity;
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
    public PurchasePriceEntity add(PurchasePriceEntity entity) {
        PurchasePriceEntity exist = repository.getBySupplierAndMaterial(entity.getSupplier(), entity.getMaterial());
        if (Objects.nonNull(exist)) {
            Result.FORBIDDEN_EXIST.show(exist.getSupplier().getName() + "-" + exist.getMaterial().getName() + " 采购价已存在!");
        }
        return addToDatabase(entity);
    }

    protected PurchasePriceEntity getByMaterialAndSupplier(MaterialEntity materialEntity, SupplierEntity supplierEntity) {
        PurchasePriceEntity exist = repository.getBySupplierAndMaterial(supplierEntity, materialEntity);
        Result.DATA_NOT_FOUND.whenNull(exist, "没有查询到该物料在此供应商下提供的采购价格，请参考物料标准采购价填写。");
        return exist;
    }
}
