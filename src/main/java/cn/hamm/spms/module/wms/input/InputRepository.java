package cn.hamm.spms.module.wms.input;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface InputRepository extends BaseBillRepository<InputEntity, InputDetailEntity> {
}
