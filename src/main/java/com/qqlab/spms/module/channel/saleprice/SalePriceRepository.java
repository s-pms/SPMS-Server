package com.qqlab.spms.module.channel.saleprice;

import com.qqlab.spms.base.BaseRepository;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.channel.customer.CustomerEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface SalePriceRepository extends BaseRepository<SalePriceEntity> {
    /**
     * 获取指定客户和物料的销售价
     *
     * @param customerEntity 客户
     * @param materialEntity 物料
     * @return 采购价实体
     */
    SalePriceEntity getByCustomerAndMaterial(CustomerEntity customerEntity, MaterialEntity materialEntity);
}
