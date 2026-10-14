package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.mes.MesServices;
import cn.hamm.spms.module.mes.order.OrderEntity;
import cn.hamm.spms.module.mes.order.OrderService;
import cn.hamm.spms.module.mes.order.enums.OrderType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * <h1>订单与计划的关联一致性</h1>
 * <p>
 * {@code OrderEntity.type} 的列默认值是 1，而 {@code OrderType.PLAN} 就是 1，
 * 也就是说<b>不传 type 的订单默认就是「计划订单」</b>；但 {@code plan} 字段可空，
 * 且后端没有任何地方给 type 赋值。
 * </p>
 * <p>
 * 两者本应表达同一个事实（有没有计划），却各存一份，于是可以互相矛盾：
 * {@code type=计划订单} 而 {@code plan=null}。原先的判定用的是 type，
 * 完结订单走到计划回写就空指针 —— 表现为「订单明明能做完，却报
 * {@code Cannot invoke "PlanEntity.getId()"}」。
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class OrderPlanConsistencyTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private MaterialService materialService;

    /**
     * 建一张订单并审核，使其处于可完结状态
     *
     * @param finishQuantity 已报工数量，0 表示还没报工
     * @param configure      额外的字段设置
     * @return 订单
     */
    private OrderEntity auditedOrder(double finishQuantity, java.util.function.Consumer<OrderEntity> configure) {
        OrderEntity order = new OrderEntity()
                .setMaterial(materialService.get(1L))
                .setQuantity(10D);
        configure.accept(order);
        order = orderService.addAndGet(order);
        var auditing = orderService.get(order.getId());
        orderService.setAudited(auditing);
        orderService.updateToDatabase(auditing);
        order = orderService.get(order.getId());
        if (finishQuantity > 0) {
            orderService.updateToDatabase(order
                    .setFinishQuantity(finishQuantity)
                    .setNgQuantity(finishQuantity));
        }
        return orderService.get(order.getId());
    }

    @Test
    @DisplayName("不挂计划单的订单（type 走默认值）完结时不应空指针")
    public void finishingOrderWithoutPlanDoesNotThrow() {
        OrderEntity order = auditedOrder(10D, o -> {
            // 刻意不设 type、不设 plan —— 这是新建订单最常见的路径
        });
        log.info("订单 {} 的 type = {}（未显式设置），plan = {}",
                order.getId(), order.getType(), order.getPlan());

        assertDoesNotThrow(() -> orderService.setOrderFinishedManually(order.getId()),
                "没挂计划单的订单完结时不应抛异常");
        log.info("完结成功，状态 = {}", orderService.get(order.getId()).getStatus());
    }

    @Test
    @DisplayName("type 应由 plan 推导，没挂计划单就不该是「计划订单」")
    public void typeIsDerivedFromPlan() {
        OrderEntity noPlan = auditedOrder(10D, o -> {
        });
        assertEquals(OrderType.OTHER.getKey(), noPlan.getType(),
                "没挂计划单的订单不应标成「计划订单」，否则完结时会去回写不存在的计划");

        log.info("无计划订单 type = {}，符合预期", noPlan.getType());
    }

    @Test
    @DisplayName("显式传 type=计划订单 但 plan 为空时，也应降级为「其他订单」而不是崩掉")
    public void explicitPlanTypeWithoutPlanIsDowngraded() {
        OrderEntity order = auditedOrder(10D, o -> o.setType(OrderType.PLAN.getKey()));
        log.info("订单 {} 的 type = {}，plan = {}",
                order.getId(), order.getType(), order.getPlan());

        assertEquals(OrderType.OTHER.getKey(), order.getType(),
                "plan 为空时 type 应被纠正为「其他订单」，不能留下自相矛盾的数据");
        assertDoesNotThrow(() -> orderService.setOrderFinishedManually(order.getId()));
    }

    @Test
    @DisplayName("挂了计划单的订单，type 应为「计划订单」")
    public void orderWithPlanGetsPlanType() {
        var plan = createPlanWithDetail(10D);
        OrderEntity order = auditedOrder(10D, o -> o.setPlan(plan));

        log.info("订单 {} 挂计划 {}，type = {}", order.getId(), plan.getId(), order.getType());
        assertEquals(OrderType.PLAN.getKey(), order.getType(),
                "挂了计划单的订单应保持「计划订单」");
        assertNotNull(order.getPlan(), "计划单应已关联");
    }

    @Test
    @DisplayName("挂了计划单的订单完结后，已完成数量应回写到计划明细")
    public void finishingPlanOrderRollsUpToPlanDetail() {
        var plan = createPlanWithDetail(10D);
        var planDetail = MesServices.getPlanDetailService().getAllByBillId(plan.getId()).get(0);
        log.info("计划 {} 明细 {} 初始完成量 = {}", plan.getId(), planDetail.getId(),
                planDetail.getFinishQuantity());

        OrderEntity order = auditedOrder(10D, o -> o.setPlan(plan));
        orderService.setOrderFinishedManually(order.getId());

        var after = MesServices.getPlanDetailService().getAllByBillId(plan.getId()).stream()
                .filter(d -> d.getId().equals(planDetail.getId()))
                .findFirst()
                .orElseThrow();
        log.info("订单完结后，计划明细完成量 = {}", after.getFinishQuantity());

        assertEquals(10D, after.getFinishQuantity(),
                "计划订单完结后应把完成量回写到计划明细，当前为 " + after.getFinishQuantity());
    }

    /**
     * 建一张含单条明细的生产计划
     *
     * @param quantity 计划数量
     * @return 生产计划
     */
    private cn.hamm.spms.module.mes.plan.PlanEntity createPlanWithDetail(double quantity) {
        var planService = MesServices.getPlanService();
        cn.hamm.spms.module.mes.plan.PlanEntity plan = planService.addAndGet(
                new cn.hamm.spms.module.mes.plan.PlanEntity());
        var auditing = planService.get(plan.getId());
        planService.setAudited(auditing);
        planService.updateToDatabase(auditing);
        MesServices.getPlanDetailService().saveDetails(plan.getId(),
                java.util.List.of(new cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity()
                        .setMaterial(materialService.get(1L))
                        .setQuantity(quantity)));
        return planService.get(plan.getId());
    }
}
