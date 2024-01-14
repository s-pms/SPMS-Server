package com.qqlab.spms.base.bill;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.security.Permission;
import com.qqlab.spms.base.BaseController;
import com.qqlab.spms.base.bill.detail.BaseBillDetailEntity;
import com.qqlab.spms.base.bill.detail.BaseBillDetailRepository;
import com.qqlab.spms.base.bill.detail.BaseBillDetailService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>单据控制器基类</h1>
 *
 * @param <E>  单据实体
 * @param <S>  单据Service
 * @param <R>  单据数据源
 * @param <D>  明细实体
 * @param <DS> 明细Service
 * @param <DR> 明细数据源
 * @author Hamm
 */
@Permission
public class BaseBillController<
        E extends AbstractBaseBillEntity<E, D>, S extends AbstractBaseBillService<E, R, D, DS, DR>, R extends BaseBillRepository<E, D>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>, DR extends BaseBillDetailRepository<D>
        > extends BaseController<E, S, R> {

    @Description("审核")
    @PostMapping("audit")
    @Filter(RootEntity.WhenGetDetail.class)
    public Json audit(@RequestBody @Validated(RootEntity.WhenIdRequired.class) E bill) {
        E savedBill = service.get(bill.getId());
        Result.FORBIDDEN.when(service.isAudited(savedBill), "该单据状态无法审核");
        service.setAudited(savedBill);
        savedBill = service.update(savedBill);
        afterAudit(savedBill);
        return json("审核成功");
    }

    @Description("驳回")
    @PostMapping("reject")
    @Filter(RootEntity.WhenGetDetail.class)
    public Json reject(@RequestBody @Validated(AbstractBaseBillEntity.WhenReject.class) E bill) {
        E savedBill = service.get(bill.getId());
        Result.FORBIDDEN.when(!service.canReject(savedBill), "该单据状态无法驳回");
        savedBill.setRejectReason(bill.getRejectReason());
        service.setReject(savedBill);
        service.update(savedBill);
        return json("驳回成功");
    }

    @Description("添加完成数量")
    @PostMapping("addFinish")
    @Filter(RootEntity.WhenGetDetail.class)
    public Json finish(@RequestBody @Validated(BaseBillDetailEntity.WhenAddFinish.class) D detail) {
        service.addFinish(detail);
        return json("添加完成数量成功");
    }


    @Override
    protected E beforeUpdate(E bill) {
        E savedBill = service.get(bill.getId());
        Result.FORBIDDEN.when(!service.canEdit(savedBill), "该单据状态下无法编辑");
        service.setAuditing(bill.setStatus(null));
        return bill;
    }

    @Override
    protected E beforeAdd(E entity) {
        return super.beforeAdd(entity.setStatus(null));
    }

    /**
     * 审核后置方法
     *
     * @param bill 单据
     */
    public void afterAudit(E bill) {
    }
}