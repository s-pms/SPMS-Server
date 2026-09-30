package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.ChannelServices;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.enums.SaleStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P2 修复验证 · 单据基类状态机（P2-1 / 2 / 4 / 14 / 18）</h1>
 * <p>
 * 这批修复落在 {@code cn.hamm.spms.base} 包下的基类里，
 * 影响全部 8 种单据，因此这里统一用「销售单」作为样本验证。
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P2BillGuardVerifyTest {

    @Autowired
    private SaleService saleService;
    @Autowired
    private MaterialService materialService;

    private SaleEntity newAuditingSale() {
        MaterialEntity material = materialService.get(1L);
        // 销售单完成后会自动生成出库单（P0-5 修复），必须先备库存
        var storage = cn.hamm.spms.module.factory.FactoryServices.getStorageService()
                .filter(null).get(0);
        var inventoryService = cn.hamm.spms.module.wms.WmsServices.getInventoryService();
        if (inventoryService.getByMaterialIdAndStorageId(material.getId(), storage.getId()) == null) {
            inventoryService.addAndGet(new cn.hamm.spms.module.wms.inventory.InventoryEntity()
                    .setMaterial(material)
                    .setStorage(storage)
                    .setType(cn.hamm.spms.module.wms.inventory.enums.InventoryType.STORAGE.getKey())
                    .setQuantity(1000D));
        }
        return saleService.addAndGet(new SaleEntity()
                .setStatus(SaleStatus.AUDITING.getKey())
                .setCustomer(ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(storage)
                .setDetails(List.of(new SaleDetailEntity()
                        .setMaterial(material).setQuantity(10D).setPrice(100D))));
    }

    private SaleEntity audit(SaleEntity sale) {
        SaleEntity auditing = saleService.get(sale.getId());
        saleService.setAudited(auditing);
        saleService.updateToDatabase(auditing);
        return saleService.get(sale.getId());
    }

    // ==================== P2-1 完成路径状态守卫 ====================

    @Test
    @DisplayName("P2-1 审核中的单据不再能被直接标记为明细完成")
    public void auditingBillCannotBeMarkedFinished() {
        SaleEntity sale = newAuditingSale();
        assertEquals(SaleStatus.AUDITING.getKey(), sale.getStatus(), "前置：应处于审核中");
        Throwable thrown = assertThrows(Exception.class,
                () -> saleService.setBillDetailsAllFinished(sale.getId()),
                "「审核中」的单据被直接推到完成态，正是 P2-1 要修的漏洞");
        log.info("P2-1 通过：审核中单据被拒 -> {}", thrown.getMessage());
    }

    @Test
    @DisplayName("P2-1 已驳回的单据也不能被直接标记完成")
    public void rejectedBillCannotBeMarkedFinished() {
        SaleEntity sale = newAuditingSale();
        // reject() 在基类里是 protected，这里直接落库目标状态模拟「已驳回」
        saleService.updateToDatabase(new SaleEntity()
                .setId(sale.getId())
                .setStatus(SaleStatus.REJECTED.getKey()));
        SaleEntity rejected = saleService.get(sale.getId());
        log.info("驳回后状态 = {}", rejected.getStatus());
        assertThrows(Exception.class,
                () -> saleService.setBillDetailsAllFinished(sale.getId()),
                "「已驳回」的单据不应能被标记完成");
        log.info("P2-1 通过：已驳回单据被拒");
    }

    @Test
    @DisplayName("P2-1 已审核的单据仍能正常走到完成（守卫没有误伤）")
    public void auditedBillStillFinishes() {
        SaleEntity sale = audit(newAuditingSale());
        assertNotEquals(SaleStatus.AUDITING.getKey(), sale.getStatus(), "前置：应已离开审核中");
        assertDoesNotThrow(() -> saleService.setBillDetailsAllFinished(sale.getId()),
                "已审核的单据必须能正常完成");
        log.info("P2-1 通过：已审核单据可正常完成，完成后状态 = {}",
                saleService.get(sale.getId()).getStatus());
    }

    @Test
    @DisplayName("P2-1 幂等由受控入口保证：订单手动完成接口重复调用被拒，不会重复生成入库单")
    public void controlledEntryIsIdempotent() {
        // 基类在结构上无法做幂等：销售单/入库单的 getAuditedStatus() 与
        // getBillDetailsFinishStatus() 是同一个状态，基类区分不了
        // 「刚审核完」与「已推进过」。因此幂等必须由受控入口保证。
        // 这里验证真实业务入口 OrderService.setOrderFinishedManually。
        var orderService = cn.hamm.spms.module.mes.MesServices.getOrderService();
        var material = materialService.get(1L);
        var storage = cn.hamm.spms.module.factory.FactoryServices.getStorageService()
                .filter(null).get(0);
        var inv = cn.hamm.spms.module.wms.WmsServices.getInventoryService();
        if (inv.getByMaterialIdAndStorageId(material.getId(), storage.getId()) == null) {
            inv.addAndGet(new cn.hamm.spms.module.wms.inventory.InventoryEntity()
                    .setMaterial(material).setStorage(storage)
                    .setType(cn.hamm.spms.module.wms.inventory.enums.InventoryType.STORAGE.getKey())
                    .setQuantity(1000D));
        }
        var order = orderService.addAndGet(new cn.hamm.spms.module.mes.order.OrderEntity()
                .setStatus(cn.hamm.spms.module.mes.order.enums.OrderStatus.AUDITING.getKey())
                .setMaterial(material)
                .setQuantity(5D));
        // 审核
        var audited = orderService.get(order.getId());
        orderService.setAudited(audited);
        orderService.updateToDatabase(audited);
        log.info("审核后订单状态 = {}", orderService.get(order.getId()).getStatus());

        // 第一次手动完成
        orderService.setOrderFinishedManually(order.getId());
        Integer statusAfterFirst = orderService.get(order.getId()).getStatus();
        log.info("第一次手动完成：订单状态 = {}", statusAfterFirst);
        assertNotEquals(cn.hamm.spms.module.mes.order.enums.OrderStatus.AUDITING.getKey(), statusAfterFirst,
                "第一次应成功推进状态");

        // 第二次必须被拒 —— 这正是 P0-6「重复完成导致库存凭空翻倍」的防线
        assertThrows(Exception.class, () -> orderService.setOrderFinishedManually(order.getId()),
                "已完成订单的重复手动完成必须被拒绝，否则会重复生成入库单、库存翻倍");
        assertEquals(statusAfterFirst, orderService.get(order.getId()).getStatus(),
                "重复调用不得再改变状态");
        log.info("P2-1 通过：受控入口幂等生效，重复调用被拒且状态未变");
    }

    // ==================== P2-18 单据禁止发布 ====================

    @Test
    @DisplayName("P2-18 单据不支持发布（发布后会永久冻结且无法删除）")
    public void billCannotBePublished() {
        SaleEntity sale = newAuditingSale();
        Throwable thrown = assertThrows(Exception.class,
                () -> saleService.publish(sale.getId()),
                "单据不应能被发布");
        assertTrue(thrown.getMessage().contains("单据不支持发布"),
                "异常信息应说明原因，实际：" + thrown.getMessage());
        log.info("P2-18 通过：发布被拒 -> {}", thrown.getMessage());
    }

    // ==================== P2-14 明细批量删除 ====================

    @Test
    @DisplayName("P2-14 deleteAllByBillId 一次清空全部明细（不再逐条删除）")
    public void deleteAllDetailsInOneShot() {
        SaleEntity sale = newAuditingSale();
        var detailService = ChannelServices.getSaleDetailService();
        assertTrue(detailService.getAllByBillId(sale.getId()).size() >= 1, "前置：应存在明细");

        detailService.deleteAllByBillId(sale.getId());
        assertEquals(0, detailService.getAllByBillId(sale.getId()).size(),
                "批量删除后应不剩任何明细（修复前 N+1 删除可能残留）");
        log.info("P2-14 通过：明细批量删除后剩余 0 条");
    }

    @Test
    @DisplayName("P2-14 删除不存在的单据明细不会抛异常")
    public void deleteDetailsOfEmptyBillIsSafe() {
        var detailService = ChannelServices.getSaleDetailService();
        SaleEntity empty = saleService.addAndGet(new SaleEntity()
                .setStatus(SaleStatus.AUDITING.getKey())
                .setCustomer(ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(cn.hamm.spms.module.factory.FactoryServices.getStorageService()
                        .filter(null).get(0))
                .setDetails(List.of()));
        assertDoesNotThrow(() -> detailService.deleteAllByBillId(empty.getId()));
        log.info("P2-14 通过：删除无明细单据不抛异常");
    }

    // ==================== P2-2 空明细不算全部完成 ====================

    @Test
    @DisplayName("P2-2 明细被清空后，单据不会被判定为「全部完成」")
    public void emptyDetailsDoNotMeanFinished() {
        SaleEntity sale = audit(newAuditingSale());
        var detailService = ChannelServices.getSaleDetailService();
        // 先把明细全删掉，模拟「源单明细被清空」
        detailService.deleteAllByBillId(sale.getId());
        assertEquals(0, detailService.getAllByBillId(sale.getId()).size(), "前置：明细已清空");

        // 再走一次分配流程（updateDetailQuantity 内部会判断是否全部完成）
        Integer statusBefore = saleService.get(sale.getId()).getStatus();
        saleService.setBillDetailsAllFinished(sale.getId());
        Integer statusAfter = saleService.get(sale.getId()).getStatus();
        log.info("P2-2 明细为 0 时：状态 {} -> {}", statusBefore, statusAfter);
        assertNotEquals(SaleStatus.DONE.getKey(), statusAfter,
                "0 明细的单据不应被推进到已完成（修复前会连锁生成 0 明细的下游单据）");
        log.info("P2-2 通过：0 明细单据未被推进到完成态");
    }
}
