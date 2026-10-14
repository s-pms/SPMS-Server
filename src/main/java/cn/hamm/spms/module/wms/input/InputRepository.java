package cn.hamm.spms.module.wms.input;

import cn.hamm.spms.base.bill.BaseBillRepository;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>入库单</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface InputRepository extends BaseBillRepository<InputEntity, InputDetailEntity> {
}
