package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import cn.hamm.spms.module.channel.customer.CustomerService;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.channel.sale.enums.SaleStatus;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.storage.StorageService;
import cn.hamm.spms.module.mes.order.OrderEntity;
import cn.hamm.spms.module.mes.order.OrderService;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.enums.OrderStatus;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.enums.InventoryType;
import cn.hamm.spms.module.wms.output.OutputEntity;
import cn.hamm.spms.module.wms.output.OutputService;
import cn.hamm.spms.module.wms.output.detail.OutputDetailEntity;
import cn.hamm.spms.module.wms.output.enums.OutputStatus;
import cn.hamm.spms.module.wms.output.enums.OutputType;
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
 * <h1>P0 修复验证 · 单据与库存类（P0-4 / P0-5 / P0-6 / P0-7）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P0BillVerifyTest {

    @Autowired
    private MaterialService materialService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private OutputService outputService;
    @Autowired
    private SaleService saleService;
    @Autowired
    private OrderService orderService;

    private MaterialEntity material() {
        // filter() 返回的实体可能已被 excludeNotMeta 处理，id 不可靠；统一按 id 取
        List<MaterialEntity> list = materialService.filter(null);
        assertTrue(!list.isEmpty(), "dev 初始化应已有物料");
        MaterialEntity first = list.get(0);
        MaterialEntity reloaded = materialService.get(first.getId() == null ? 1L : first.getId());
        assertNotNull(reloaded.getId(), "物料 id 必须可用");
        return reloaded;
    }

    private StorageEntity storage(int index) {
        List<StorageEntity> list = storageService.filter(null);
        assertTrue(list.size() > index, "dev 初始化应已有足够仓库");
        return list.get(index);
    }

    private CustomerEntity customer() {
        return customerService.filter(null).get(0);
    }

    private InventoryEntity newInventory(StorageEntity storage, double quantity) {
        InventoryEntity exist = inventoryService.getByMaterialIdAndStorageId(
                material().getId(), storage.getId());
        if (exist != null) {
            inventoryService.addInventoryQuantity(exist.getId(), quantity);
            return exist;
        }
        return inventoryService.addAndGet(new InventoryEntity()
                .setMaterial(material())
                .setStorage(storage)
                .setType(InventoryType.STORAGE.getKey())
                .setQuantity(quantity));
    }

    // ==================== P0-4 ====================

    @Test
    @DisplayName("P0-4 出库报工只扣明细入库时绑定的库存行，客户端临时换库存行无效")
    public void outputUsesInventoryBoundInDatabase() {
        StorageEntity s1 = storage(0);
        StorageEntity s2 = storage(1);
        InventoryEntity bound = newInventory(s1, 100D);
        InventoryEntity other = newInventory(s2, 100D);

        OutputEntity bill = outputService.addAndGet(new OutputEntity()
                .setStatus(OutputStatus.AUDITING.getKey())
                .setType(OutputType.NORMAL.getKey())
                .setDetails(List.of(new OutputDetailEntity()
                        .setInventory(bound)
                        .setQuantity(10D))));
        OutputEntity audited = outputService.get(bill.getId());
        outputService.setAudited(audited);
        outputService.updateToDatabase(audited);

        List<OutputDetailEntity> details = cn.hamm.spms.module.wms.WmsServices.getOutputDetailService().getAllByBillId(bill.getId());
        OutputDetailEntity detail = details.get(0);
        // 模拟客户端在报工请求里把 inventory 换成另一条库存行
        OutputDetailEntity source = new OutputDetailEntity()
                .setId(detail.getId())
                .setQuantity(10D)
                .setInventory(other)
                .setMaterial(material());

        double boundBefore = inventoryService.get(bound.getId()).getQuantity();
        double otherBefore = inventoryService.get(other.getId()).getQuantity();

        outputService.addDetailFinishQuantity(source);

        assertEquals(boundBefore - 10D, inventoryService.get(bound.getId()).getQuantity(),
                "应当扣减明细中已保存的库存行");
        assertEquals(otherBefore, inventoryService.get(other.getId()).getQuantity(),
                "客户端指定的库存行不应被扣减");
        log.info("P0-4 通过：绑定库存 {} {} -> {}，客户端指定库存 {} 保持 {}",
                bound.getId(), boundBefore, boundBefore - 10D, other.getId(), otherBefore);
    }

    @Test
    @DisplayName("P0-4 明细未关联库存行时报出明确业务异常，而不是 NPE")
    public void outputWithoutInventoryFailsClearly() {
        OutputEntity bill = outputService.addAndGet(new OutputEntity()
                .setStatus(OutputStatus.AUDITING.getKey())
                .setType(OutputType.NORMAL.getKey())
                .setDetails(List.of(new OutputDetailEntity()
                        .setQuantity(5D))));
        OutputEntity audited = outputService.get(bill.getId());
        outputService.setAudited(audited);
        outputService.updateToDatabase(audited);
        OutputDetailEntity detail = cn.hamm.spms.module.wms.WmsServices.getOutputDetailService().getAllByBillId(bill.getId()).get(0);
        OutputDetailEntity source = new OutputDetailEntity().setId(detail.getId()).setQuantity(5D);
        Throwable thrown = assertThrows(Exception.class, () -> outputService.addDetailFinishQuantity(source));
        assertTrue(!(thrown instanceof NullPointerException),
                "不应抛 NPE，实际: " + thrown.getClass().getName());
        log.info("P0-4 边界通过：{}", thrown.getMessage());
    }

    // ==================== P0-5 ====================

    @Test
    @DisplayName("P0-5 销售单明细全部完成后自动生成出库单（修复前库存永不减少）")
    public void saleGeneratesOutputBill() {
        StorageEntity s = storage(0);
        MaterialEntity m = material();
        newInventory(s, 200D);
        SaleEntity sale = saleService.addAndGet(new SaleEntity()
                .setStatus(SaleStatus.AUDITING.getKey())
                .setCustomer(customer())
                .setStorage(s)
                .setDetails(List.of(new SaleDetailEntity()
                        .setMaterial(m)
                        .setQuantity(20D)
                        .setPrice(100D))));
        SaleEntity audited = saleService.get(sale.getId());
        saleService.setAudited(audited);
        saleService.updateToDatabase(audited);

        var detail = cn.hamm.spms.module.channel.ChannelServices.getSaleDetailService().getAllByBillId(sale.getId()).get(0);
        saleService.addDetailFinishQuantity(new SaleDetailEntity()
                .setId(detail.getId())
                .setQuantity(20D));

        List<OutputEntity> outputs = outputService.filter(null).stream()
                .filter(o -> o.getSale() != null && o.getSale().getId().equals(sale.getId()))
                .toList();
        assertEquals(1, outputs.size(), "应自动生成 1 张销售出库单");
        OutputEntity output = outputs.get(0);
        assertEquals(OutputType.SALE.getKey(), output.getType());
        log.info("P0-5 通过：销售单 {} 生成出库单 {}，状态={}",
                sale.getId(), output.getId(), output.getStatus());
    }

    @Test
    @DisplayName("P0-5 未指定发货仓库时给出明确提示，不静默跳过出库")
    public void saleWithoutStorageIsRejected() {
        SaleEntity sale = saleService.addAndGet(new SaleEntity()
                .setStatus(SaleStatus.AUDITING.getKey())
                .setCustomer(customer())
                .setDetails(List.of(new SaleDetailEntity()
                        .setQuantity(1D)
                        .setPrice(10D))));
        SaleEntity audited = saleService.get(sale.getId());
        saleService.setAudited(audited);
        saleService.updateToDatabase(audited);
        var detail = cn.hamm.spms.module.channel.ChannelServices.getSaleDetailService().getAllByBillId(sale.getId()).get(0);
        Throwable thrown = assertThrows(Exception.class, () -> saleService.addDetailFinishQuantity(
                new SaleDetailEntity().setId(detail.getId()).setQuantity(1D)));
        log.info("P0-5 边界通过：{}", thrown.getMessage());
        assertNotNull(thrown);
    }

    // ==================== P0-6 ====================

    @Test
    @DisplayName("P0-6 手动标记完成幂等：第二次调用被状态守卫拒绝，不会重复建入库单")
    public void manualFinishIsIdempotent() {
        OrderEntity order = newOrder(100D);
        orderService.setOrderFinishedManually(order.getId());
        Integer statusAfterFirst = orderService.get(order.getId()).getStatus();
        log.info("P0-6 首���调用后状态 = {}", statusAfterFirst);

        Throwable thrown = assertThrows(Exception.class,
                () -> orderService.setOrderFinishedManually(order.getId()));
        log.info("P0-6 通过：重复调用被拒绝 -> {}", thrown.getMessage());
        assertEquals(statusAfterFirst, orderService.get(order.getId()).getStatus(),
                "状态不应被二次推进");
    }

    @Test
    @DisplayName("P0-6 状态守卫：审核中的订单不允许手动标记完成")
    public void manualFinishRejectsAuditingOrder() {
        OrderEntity order = auditingOrder(50D);
        Throwable thrown = assertThrows(Exception.class,
                () -> orderService.setOrderFinishedManually(order.getId()));
        log.info("P0-6 状态守卫通过：{}", thrown.getMessage());
        assertNotNull(thrown);
    }

    // ==================== P0-7 ====================

    @Test
    @DisplayName("P0-7 报工超过订单剩余数量被拒绝")
    public void reportQuantityExceedingRemainIsRejected() {
        OrderEntity order = newOrder(10D);
        orderService.addOrderDetail(new OrderDetailEntity()
                .setBillId(order.getId())
                .setQuantity(6D));
        Throwable thrown = assertThrows(Exception.class, () -> orderService.addOrderDetail(
                new OrderDetailEntity()
                        .setBillId(order.getId())
                        .setQuantity(5D)));
        log.info("P0-7 数量上限通过：{}", thrown.getMessage());
        assertNotNull(thrown);
    }

    @Test
    @DisplayName("P0-7 报工数量必须大于 0")
    public void reportNonPositiveQuantityIsRejected() {
        OrderEntity order = newOrder(10D);
        Throwable thrown = assertThrows(Exception.class, () -> orderService.addOrderDetail(
                new OrderDetailEntity()
                        .setBillId(order.getId())
                        .setQuantity(0D)));
        log.info("P0-7 数量下限通过：{}", thrown.getMessage());
        assertNotNull(thrown);
    }

    @Test
    @DisplayName("P0-7 连续报工累加正确，订单完成数量与明细一致")
    public void consecutiveReportsAccumulate() {
        OrderEntity order = newOrder(100D);
        orderService.addOrderDetail(new OrderDetailEntity()
                .setBillId(order.getId()).setQuantity(30D));
        orderService.addOrderDetail(new OrderDetailEntity()
                .setBillId(order.getId()).setQuantity(20D));
        OrderEntity reloaded = orderService.get(order.getId());
        assertEquals(50D, reloaded.getFinishQuantity(), "订单完成数量应为两次报工之和");
        log.info("P0-7 累加通过：订单 {} finishQuantity={}", order.getId(), reloaded.getFinishQuantity());
    }

    @Test
    @DisplayName("P0-7 状态守卫：审核中的订单不允许报工")
    public void reportOnAuditingOrderIsRejected() {
        OrderEntity order = auditingOrder(10D);
        Throwable thrown = assertThrows(Exception.class, () -> orderService.addOrderDetail(
                new OrderDetailEntity()
                        .setBillId(order.getId()).setQuantity(1D)));
        log.info("P0-7 状态守卫通过：{}", thrown.getMessage());
        assertNotNull(thrown);
    }

    private OrderEntity newOrder(double quantity) {
        OrderEntity order = auditingOrder(quantity);
        OrderEntity audited = orderService.get(order.getId());
        orderService.setAudited(audited);
        orderService.updateToDatabase(audited);
        return order;
    }

    private OrderEntity auditingOrder(double quantity) {
        return orderService.addAndGet(new OrderEntity()
                .setStatus(OrderStatus.AUDITING.getKey())
                .setMaterial(material())
                .setQuantity(quantity));
    }
}
