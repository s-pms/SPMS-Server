package cn.hamm.spms.base.bill.detail;

import cn.hamm.airpower.annotation.Permission;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>单据控制器基类</h1>
 *
 * @param <E> 明细实体
 * @param <S> 明细Service
 * @param <R> 明细数据源
 * @author Hamm.cn
 */
@Permission
public class BaseBillDetailController<
        E extends BaseBillDetailEntity, S extends BaseBillDetailService<E, R>, R extends BaseBillDetailRepository<E>
        > extends BaseController<E, S, R> {
}
