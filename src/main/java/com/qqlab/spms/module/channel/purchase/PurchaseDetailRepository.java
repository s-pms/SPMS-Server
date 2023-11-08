package com.qqlab.spms.module.channel.purchase;

import cn.hamm.airpower.root.RootRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface PurchaseDetailRepository extends RootRepository<PurchaseDetailEntity> {

    /**
     * 查询指定采购单的所有明细
     *
     * @param purchaseId 采购单ID
     * @return 明细
     */
    List<PurchaseDetailEntity> getAllByPurchaseId(Long purchaseId);
}
