package cn.hamm.spms.module.channel.purchaseprice;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.supplier.SupplierEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PurchasePriceRepository extends BaseRepository<PurchasePriceEntity> {
    /**
     * 获取指定供应商和物料的采购价
     *
     * @param supplier 供应商
     * @param material 物料
     * @return 采购价实体
     */
    PurchasePriceEntity getBySupplierAndMaterial(SupplierEntity supplier, MaterialEntity material);
}
