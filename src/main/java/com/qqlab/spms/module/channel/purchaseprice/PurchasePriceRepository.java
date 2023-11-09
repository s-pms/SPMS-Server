package com.qqlab.spms.module.channel.purchaseprice;

import com.qqlab.spms.base.BaseRepository;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.channel.supplier.SupplierEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface PurchasePriceRepository extends BaseRepository<PurchasePriceEntity> {
    /**
     * <h2>获取指定供应商和物料的采购价</h2>
     * @param supplierEntity 供应商
     * @param materialEntity 物料
     * @return 采购价实体
     */
    PurchasePriceEntity getBySupplierAndMaterial(SupplierEntity supplierEntity, MaterialEntity materialEntity);
}
