package com.qqlab.spms.module.basic.saleprice;

import cn.hamm.airpower.root.RootRepository;
import com.qqlab.spms.module.basic.customer.CustomerEntity;
import com.qqlab.spms.module.basic.material.MaterialEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface SalePriceRepository extends RootRepository<SalePriceEntity> {
    /**
     * <h2>获取指定客户和物料的销售价</h2>
     * @param customerEntity 客户
     * @param materialEntity 物料
     * @return 采购价实体
     */
    SalePriceEntity getByCustomerAndMaterial(CustomerEntity customerEntity, MaterialEntity materialEntity);
}
