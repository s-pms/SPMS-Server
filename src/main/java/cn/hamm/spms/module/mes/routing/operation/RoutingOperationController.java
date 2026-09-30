package cn.hamm.spms.module.mes.routing.operation;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.curd.annotation.Extends;
import cn.hamm.airpower.curd.base.Curd;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.exception.Errors.API_SERVICE_UNSUPPORTED;

/**
 * <h1>工序配置</h1>
 * <p>
 * 工序是工艺主单（{@code routing}）的明细，正确的维护路径是
 * {@code POST /routing/add} 与 {@code POST /routing/update} 提交 {@code details} 数组，
 * 由 {@code RoutingService} 统一填充 {@code routingId} 并整体覆盖。
 * <p>
 * 这里不开放增删改：若允许独立增删改，{@code routingId} 就是客户端可写且无外键校验的字段，
 * 可以给不存在的工艺挂工序、把工艺 A 的工序搬到工艺 B、或单独删掉某道工序而主单毫无感知，
 * 最终绕过 {@code RoutingService.beforePublish} 的「工艺没有任何工序流程」校验。
 *
 * @author zfy
 */
@RestController
@Description("工序配置")
@Api("routingOperation")
@Extends(exclude = {Curd.Add, Curd.Update, Curd.Delete})
public class RoutingOperationController extends BaseController<RoutingOperationEntity, RoutingOperationService, RoutingOperationRepository> {

    /**
     * 工序不支持单独发布
     * <p>
     * 发布会把该条工序标记为不可修改，而工艺主单保存时是「先按 routingId 全删再全建」，
     * 单独发布过的工序会造成新旧工序混杂的中间态。
     * </p>
     *
     * @param entity 仅取其 ID
     * @return 不会返回，必定抛异常
     */
    @Override
    @Description("发布（工序不支持单独发布）")
    @PostMapping("publish")
    public Json publish(@RequestBody @Validated(WhenIdRequired.class) RoutingOperationEntity entity) {
        API_SERVICE_UNSUPPORTED.show("工序不支持单独发布，请在工艺主单中维护");
        return Json.success("该接口已禁用");
    }
}
