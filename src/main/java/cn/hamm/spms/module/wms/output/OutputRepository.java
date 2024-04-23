package cn.hamm.spms.module.wms.output;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface OutputRepository extends BaseBillRepository<OutputEntity, OutputDetailEntity> {
}
