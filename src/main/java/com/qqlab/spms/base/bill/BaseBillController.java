package com.qqlab.spms.base.bill;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.ResponseFilter;
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
        E extends BaseBillEntity<E, D>, S extends AbstractBaseBillService<E, R, D, DS, DR>, R extends BaseBillRepository<E, D>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>, DR extends BaseBillDetailRepository<D>
        > extends BaseController<E, S, R> {

    @Description("审核")
    @PostMapping("audit")
    @ResponseFilter(RootEntity.WhenGetDetail.class)
    public Json audit(@RequestBody @Validated(RootEntity.WhenIdRequired.class) E bill) {
        E savedBill = service.getById(bill.getId());
        Result.FORBIDDEN.when(service.isAudited(savedBill), "该单据状态无法审核");
        service.setAudited(savedBill);
        service.update(savedBill);
        return json("审核成功");
    }

    @Description("驳回")
    @PostMapping("reject")
    @ResponseFilter(RootEntity.WhenGetDetail.class)
    public Json reject(@RequestBody @Validated(BaseBillEntity.WhenReject.class) E bill) {
        E savedBill = service.getById(bill.getId());
        Result.FORBIDDEN.when(!service.canReject(savedBill), "该单据状态无法驳回");
        savedBill.setRejectReason(bill.getRejectReason());
        service.setReject(savedBill);
        service.update(savedBill);
        return json("驳回成功");
    }

    @Override
    protected E beforeUpdate(E bill) {
        E savedBill = service.getById(bill.getId());
        Result.FORBIDDEN.when(!service.canEdit(savedBill), "该单据状态下无法编辑");
        service.setAuditing(savedBill);
        return savedBill;
    }
}