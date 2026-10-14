package cn.hamm.spms.module.wms.output.detail;

import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>出库明细</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface OutputDetailRepository extends BaseBillDetailRepository<OutputDetailEntity> {
}
