package cn.hamm.spms.base.bill;

import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.api.fiter.Filter;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailService;
import cn.hamm.spms.base.bill.detail.IBaseBillDetailAction;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;

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
@Slf4j
@Permission
public class BaseBillController<
        E extends AbstractBaseBillEntity<E, D>, S extends AbstractBaseBillService<E, R, D, DS, DR>, R extends BaseBillRepository<E, D>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>, DR extends BaseBillDetailRepository<D>
        > extends BaseController<E, S, R> implements IBaseBillAction, IBaseBillDetailAction {

    @Autowired(required = false)
    protected DS detailService;

    @Description("审核")
    @PostMapping("audit")
    @Filter(WhenGetDetail.class)
    public Json audit(@RequestBody @Validated(WhenIdRequired.class) E bill) {
        service.audit(bill.getId());
        E savedBill = service.get(bill.getId());
        afterAudit(savedBill);
        return Json.success("审核成功");
    }

    @Description("设置所有明细已完成")
    @PostMapping("setBillDetailsAllFinished")
    @Filter(WhenGetDetail.class)
    public Json setBillDetailsAllFinished(@RequestBody @Validated(WhenIdRequired.class) E bill) {
        service.setBillDetailsAllFinished(bill.getId());
        return Json.success("设置所有明细已完成成功");
    }

    @Description("设置单据已完成")
    @PostMapping("setBillFinished")
    @Filter(WhenGetDetail.class)
    public Json setBillFinished(@RequestBody @Validated(WhenIdRequired.class) E bill) {
        service.setBillFinished(bill.getId());
        return Json.success("设置单据已完成成功");
    }

    @Description("驳回")
    @PostMapping("reject")
    @Filter(WhenGetDetail.class)
    public Json reject(@RequestBody @Validated(WhenReject.class) E bill) {
        E savedBill = service.get(bill.getId());
        FORBIDDEN.when(!service.canReject(savedBill), "该单据状态无法驳回");
        savedBill.setRejectReason(bill.getRejectReason());
        service.setReject(savedBill);
        service.update(savedBill);
        return Json.success("驳回成功");
    }

    @Description("添加明细的完成数量")
    @PostMapping("addFinish")
    @Filter(WhenGetDetail.class)
    public Json addFinish(@RequestBody @Validated(WhenAddFinish.class) D detail) {
        service.addDetailFinishQuantity(detail);
        return Json.success("添加完成数量成功");
    }

    @Override
    protected final @NotNull E beforeAppUpdate(@NotNull E bill) {
        E exist = service.get(bill.getId());
        FORBIDDEN.when(!service.canEdit(exist), "该单据状态下无法编辑");
        service.setAuditing(exist.setStatus(null));
        return exist;
    }

    @Override
    protected final E beforeAdd(@NotNull E bill) {
        return super.beforeAdd(bill.setStatus(null));
    }

    /**
     * 审核后置方法
     *
     * @param bill 单据
     */
    protected void afterAudit(@NotNull E bill) {
        log.info("单据审核成功，单据ID：{}", bill.getId());
    }
}
