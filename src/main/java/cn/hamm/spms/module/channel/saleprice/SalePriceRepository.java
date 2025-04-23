package cn.hamm.spms.module.channel.saleprice;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface SalePriceRepository extends BaseRepository<SalePriceEntity> {
    /**
     * 获取指定客户和物料的销售价
     *
     * @param customer 客户
     * @param material 物料
     * @return 采购价实体
     */
    SalePriceEntity getByCustomerAndMaterial(CustomerEntity customer, MaterialEntity material);
}
