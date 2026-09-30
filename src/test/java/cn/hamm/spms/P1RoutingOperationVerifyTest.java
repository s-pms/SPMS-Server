package cn.hamm.spms;

import cn.hamm.airpower.core.Json;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.mes.operation.OperationEntity;
import cn.hamm.spms.module.mes.operation.OperationService;
import cn.hamm.spms.module.mes.routing.RoutingEntity;
import cn.hamm.spms.module.mes.routing.RoutingService;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationController;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P1 修复验证 · 工序配置（P1-15）</h1>
 * <p>
 * 关键前提：框架的 {@code @Extends(exclude = ...)} <b>不会</b>让接口从 Spring 路由表里消失，
 * 它只在 {@code Curd.checkApiAvailable()} 里做运行时检查（{@code API_SERVICE_UNSUPPORTED.when(...)}）。
 * 所以这里一律通过「实际调用 Controller 方法」来验证，而不是查路由表。
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P1RoutingOperationVerifyTest {

    @Autowired
    private RoutingOperationController routingOperationController;
    @Autowired
    private RoutingService routingService;
    @Autowired
    private RoutingOperationService routingOperationService;
    @Autowired
    private MaterialService materialService;
    @Autowired
    private OperationService operationService;

    @Test
    @DisplayName("P1-15 工序的 add 接口被禁用（调用即抛「接口暂未实现」）")
    public void addIsDisabled() {
        Throwable thrown = assertThrows(Exception.class,
                () -> routingOperationController.add(
                        new RoutingOperationEntity().setRoutingId(1L)));
        assertTrue(thrown.getMessage().contains("暂未实现"),
                "应提示接口未实现，实际：" + thrown.getMessage());
        log.info("P1-15 通过：add 被拒 -> {}", thrown.getMessage());
    }

    @Test
    @DisplayName("P1-15 工序的 update 接口被禁用（不能把工序搬到别的工艺）")
    public void updateIsDisabled() {
        Throwable thrown = assertThrows(Exception.class,
                () -> routingOperationController.update(
                        new RoutingOperationEntity().setId(1L).setRoutingId(2L)));
        assertTrue(thrown.getMessage().contains("暂未实现"),
                "应提示接口未实现，实际：" + thrown.getMessage());
        log.info("P1-15 通过：update 被拒 -> {}", thrown.getMessage());
    }

    @Test
    @DisplayName("P1-15 工序的 delete 接口被禁用（单条删工序会绕过主单校验）")
    public void deleteIsDisabled() {
        Throwable thrown = assertThrows(Exception.class,
                () -> routingOperationController.delete(
                        new RoutingOperationEntity().setId(1L)));
        assertTrue(thrown.getMessage().contains("暂未实现"),
                "应提示接口未实现，实际：" + thrown.getMessage());
        log.info("P1-15 通过：delete 被拒 -> {}", thrown.getMessage());
    }

    @Test
    @DisplayName("P1-15 工序不支持单独发布，调用直接抛业务异常")
    public void publishIsDisabled() {
        Throwable thrown = assertThrows(Exception.class,
                () -> routingOperationController.publish(
                        new RoutingOperationEntity().setId(1L)),
                "单独发布工序应被拒绝");
        assertNotNull(thrown.getMessage());
        assertTrue(thrown.getMessage().contains("工序不支持单独发布"),
                "异常信息应说明原因，实际：" + thrown.getMessage());
        log.info("P1-15 通过：单独发布被拒 -> {}", thrown.getMessage());
    }

    @Test
    @DisplayName("P1-15 只读接口仍可用（禁用写接口没有误伤查询）")
    public void readApisStillWork() {
        assertDoesNotThrowGetList();
        log.info("P1-15 通过：getList 仍可正常调用");
    }

    private void assertDoesNotThrowGetList() {
        Json result = routingOperationController.getList(null);
        assertNotNull(result, "getList 应正常返回");
    }

    @Test
    @DisplayName("P1-15 工序仍可通过工艺主单正常维护（没有误伤正常业务）")
    public void operationsCanStillBeMaintainedViaRouting() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        MaterialEntity material = materialService.get(1L);
        OperationEntity operation = operationService.get(1L);

        RoutingEntity routing = routingService.addAndGet(new RoutingEntity()
                .setName("工艺-" + tag)
                .setMaterial(material)
                .setIsRoutingBom(false)
                .setDetails(List.of(
                        new RoutingOperationEntity().setOperation(operation).setOrderNo(1),
                        new RoutingOperationEntity().setOperation(operation).setOrderNo(2))));

        RoutingEntity loaded = routingService.get(routing.getId());
        assertEquals(2, loaded.getDetails().size(), "工序应通过工艺主单保存成功");
        assertTrue(loaded.getDetails().stream()
                        .allMatch(d -> d.getRoutingId().equals(routing.getId())),
                "routingId 应由服务端统一填充");

        // 关键：主单保存是「全删再全建」语义，改一次工序数应精确等于提交数
        routingService.update(new RoutingEntity()
                .setId(routing.getId())
                .setName("工艺改-" + tag)
                .setMaterial(material)
                .setIsRoutingBom(false)
                .setDetails(List.of(
                        new RoutingOperationEntity().setOperation(operation).setOrderNo(1))));
        assertEquals(1, routingOperationService
                        .filter(new RoutingOperationEntity().setRoutingId(routing.getId())).size(),
                "工序应为全量覆盖而非追加（修复前批量删除只有最后一条生效，工序会逐次累积）");

        // 再验证一次批量场景：3 条改 2 条，不能残留
        routingService.update(new RoutingEntity()
                .setId(routing.getId())
                .setName("工艺改2-" + tag)
                .setMaterial(material)
                .setIsRoutingBom(false)
                .setDetails(List.of(
                        new RoutingOperationEntity().setOperation(operation).setOrderNo(1),
                        new RoutingOperationEntity().setOperation(operation).setOrderNo(2))));
        assertEquals(2, routingOperationService
                        .filter(new RoutingOperationEntity().setRoutingId(routing.getId())).size(),
                "3 条改 2 条不应有残留");
        log.info("P1-15 通过：经工艺主单 2 道 -> 1 道 -> 2 道，工序数量始终与提交一致");
    }

    @Test
    @DisplayName("P1-15 routingId 为必填（纵深防御，防止将来重新开放接口时漏校验）")
    public void routingIdIsRequired() throws NoSuchFieldException {
        NotNull notNull = RoutingOperationEntity.class.getDeclaredField("routingId")
                .getAnnotation(NotNull.class);
        assertNotNull(notNull, "routingId 缺少 @NotNull，修复前客户端可传 null 撞出 500");
        log.info("P1-15 通过：routingId 已标注 @NotNull");
    }
}
