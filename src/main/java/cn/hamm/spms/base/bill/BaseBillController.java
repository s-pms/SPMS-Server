package cn.hamm.spms.base.bill;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.security.Permission;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailService;
import cn.hamm.spms.base.bill.detail.IBaseBillDetailAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <h1>单据控制器基类</h1>
 *
 * @param <E>  单据实体
 * @param <S>  单据Service
 * @param <R>  单据数据源
 * @param <D>  明细实体
 * @param <DS> 明细Service
 * @param <DR> 明细数据源
 * @author Hamm.cn
 */
@Permission
public class BaseBillController<
        E extends AbstractBaseBillEntity<E, D>, S extends AbstractBaseBillService<E, R, D, DS, DR>, R extends BaseBillRepository<E, D>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>, DR extends BaseBillDetailRepository<D>
        > extends BaseController<E, S, R> implements IBaseBillAction, IBaseBillDetailAction {

    @Description("审核")
    @RequestMapping("audit")
    @Filter(WhenGetDetail.class)
    public Json audit(@RequestBody @Validated(WhenIdRequired.class) E bill) {
        E savedBill = service.get(bill.getId());
        Result.FORBIDDEN.when(!service.canAudit(savedBill), "该单据状态无法审核");
        service.setAudited(savedBill);
        service.update(savedBill);
        afterAudit(savedBill);
        return json("审核成功");
    }

    @Description("驳回")
    @RequestMapping("reject")
    @Filter(WhenGetDetail.class)
    public Json reject(@RequestBody @Validated(WhenReject.class) E bill) {
        E savedBill = service.get(bill.getId());
        Result.FORBIDDEN.when(!service.canReject(savedBill), "该单据状态无法驳回");
        savedBill.setRejectReason(bill.getRejectReason());
        service.setReject(savedBill);
        service.update(savedBill);
        return json("驳回成功");
    }

    @Description("添加完成数量")
    @RequestMapping("addFinish")
    @Filter(WhenGetDetail.class)
    public Json finish(@RequestBody @Validated(WhenAddFinish.class) D detail) {
        service.addFinish(detail);
        return json("添加完成数量成功");
    }

    @Override
    protected final @NotNull E beforeUpdate(@NotNull E bill) {
        E savedBill = service.get(bill.getId());
        Result.FORBIDDEN.when(!service.canEdit(savedBill), "该单据状态下无法编辑");
        service.setAuditing(savedBill.setStatus(null));
        return savedBill;
    }


    @Override
    protected final E beforeAdd(@NotNull E bill) {
        return super.beforeAdd(bill.setStatus(null));
    }

    /**
     * <h2>审核后置方法</h2>
     *
     * @param bill 单据
     */
    protected void afterAudit(@NotNull E bill) {
    }
}