package com.qqlab.spms.module.basic.purchaseprice;

import cn.hamm.airpower.root.RootRepository;
import com.qqlab.spms.module.basic.material.MaterialEntity;
import com.qqlab.spms.module.basic.supplier.SupplierEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface PurchasePriceRepository extends RootRepository<PurchasePriceEntity> {
    /**
     * <h2>获取指定供应商和物料的采购价</h2>
     * @param supplierEntity 供应商
     * @param materialEntity 物料
     * @return 采购价实体
     */
    PurchasePriceEntity getBySupplierAndMaterial(SupplierEntity supplierEntity, MaterialEntity materialEntity);
}
