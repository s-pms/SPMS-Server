package cn.hamm.spms.base.bill;

import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.access.Permission;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.airpower.web.curd.Curd;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.base.bill.detail.BaseBillDetailEntity;
import cn.hamm.spms.base.bill.detail.BaseBillDetailRepository;
import cn.hamm.spms.base.bill.detail.BaseBillDetailService;
import cn.hamm.spms.base.bill.detail.IBaseBillDetailAction;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN;

/**
 * <h1>单据控制器基类</h1>
 *
 * @param <E>  单据实体
 * @param <S>  单据 Service
 * @param <R>  单据数据源
 * @param <D>  明细实体
 * @param <DS> 明细 Service
 * @param <DR> 明细数据源
 * @author Hamm.cn
 */
@Slf4j
@Permission
@Extends(exclude = {Curd.Delete})
public class BaseBillController<
        E extends AbstractBaseBillEntity<E, D>, S extends AbstractBaseBillService<E, R, D, DS, DR>, R extends BaseBillRepository<E, D>,
        D extends BaseBillDetailEntity<D>, DS extends BaseBillDetailService<D, DR>, DR extends BaseBillDetailRepository<D>
        > extends BaseController<E, S, R> implements IBaseBillAction, IBaseBillDetailAction {

    @Description("审核")
    @PostMapping("audit")
    public Json audit(@RequestBody @Validated(WhenIdRequired.class) E bill) {
        service.audit(bill.getId());
        return Json.success("审核成功");
    }

    @Description("驳回")
    @PostMapping("reject")
    public Json reject(@RequestBody @Validated(WhenReject.class) E bill) {
        service.reject(bill.getId());
        return Json.success("驳回成功");
    }

    @Description("添加明细的完成数量")
    @PostMapping("addFinish")
    public Json addFinish(@RequestBody @Validated(WhenAddFinish.class) D detail) {
        service.addDetailFinishQuantity(detail);
        return Json.success("添加完成数量成功");
    }

    @Contract("_ -> param1")
    @Override
    protected final @NotNull E beforeAppUpdate(@NotNull E bill) {
        E exist = service.get(bill.getId());
        FORBIDDEN.when(!service.canEdit(exist), "该单据状态下无法编辑");
        service.setAuditing(bill);
        beforeBillUpdate(bill);
        return bill;
    }

    /**
     * 单据更新前置方法
     *
     * @param bill 单据
     */
    protected void beforeBillUpdate(@NotNull E bill) {
        log.info("单据更新，单据ID：{}", bill.getId());
    }
}
