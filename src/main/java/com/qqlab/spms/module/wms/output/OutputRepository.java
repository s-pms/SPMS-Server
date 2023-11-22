package com.qqlab.spms.module.wms.output;

import com.qqlab.spms.base.bill.BaseBillRepository;
import com.qqlab.spms.module.wms.output.detail.OutputDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface OutputRepository extends BaseBillRepository<OutputEntity, OutputDetailEntity> {
}
