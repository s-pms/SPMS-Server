package com.qqlab.spms.module.channel.sale.detail;

import com.qqlab.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface SaleDetailRepository extends BaseRepository<SaleDetailEntity> {

    /**
     * 根据单据ID查询所有明细
     *
     * @param billId 单据ID
     * @return 明细
     */
    List<SaleDetailEntity> getAllByBillId(Long billId);
}
