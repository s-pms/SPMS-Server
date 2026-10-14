package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.ChannelServices;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.factory.FactoryServices;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>手动完结入口的并发防护</h1>
 * <p>
 * 覆盖两条不经报工流程、直接推进单据的路径：
 * </p>
 * <ol>
 *   <li>{@code setBillDetailsAllFinished} —— 对外可调用的受控推进接口</li>
 *   <li>{@code setOrderFinishedManually} —— 订单「任意状态强制完成」入口</li>
 * </ol>
 * <p>
 * 这两条路径绕过报工的加锁顺序，若不加保护，并发调用会重复执行
 * {@code afterAllBillDetailFinished}，生成多张下游单据，库存凭空翻倍。
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class ConcurrentManualFinishVerifyTest {

    @Autowired
    private SaleService saleService;
    @Autowired
    private MaterialService materialService;

    /**
     * 建一张已审核、含 2 行明细的销售单
     *
     * @param eachQuantity 每行数量
     * @return 销售单
     */
    private SaleEntity auditedSale(double eachQuantity) {
        MaterialEntity material = materialService.get(1L);
        var storage = FactoryServices.getStorageService().filter(null).get(0);
        var inv = WmsServices.getInventoryService();
        if (inv.getByMaterialIdAndStorageId(material.getId(), storage.getId()) == null) {
            inv.addAndGet(new cn.hamm.spms.module.wms.inventory.InventoryEntity()
                    .setMaterial(material).setStorage(storage)
                    .setType(cn.hamm.spms.module.wms.inventory.enums.InventoryType.STORAGE.getKey())
                    .setQuantity(100000D));
        }
        SaleEntity sale = saleService.addAndGet(new SaleEntity()
                .setCustomer(ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(storage)
                .setDetails(List.of(
                        new SaleDetailEntity().setMaterial(material).setQuantity(eachQuantity).setPrice(100D),
                        new SaleDetailEntity().setMaterial(material).setQuantity(eachQuantity).setPrice(100D))));
        SaleEntity auditing = saleService.get(sale.getId());
        saleService.setAudited(auditing);
        saleService.updateToDatabase(auditing);
        return saleService.get(sale.getId());
    }

    /**
     * 统计某张销售单生成的出库单数
     *
     * @param saleId 销售单 ID
     * @return 出库单数
     */
    private long outputCount(long saleId) {
        return WmsServices.getOutputService().filter(null).stream()
                .filter(o -> o.getSale() != null && o.getSale().getId() == saleId)
                .count();
    }

    @Test
    @DisplayName("并发调用 setBillDetailsAllFinished：出库单只能有 1 张")
    public void concurrentSetBillDetailsAllFinishedGeneratesOneOutput() throws Exception {
        SaleEntity sale = auditedSale(10D);
        log.info("并发推进单据 ID = {}，初始状态 = {}", sale.getId(), sale.getStatus());

        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        CountDownLatch gate = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    gate.await(5, TimeUnit.SECONDS);
                    saleService.setBillDetailsAllFinished(sale.getId());
                    success.incrementAndGet();
                } catch (Exception e) {
                    // 并发下有一方被状态守卫拒绝是预期的，关键是下游单据不能重复
                    rejected.incrementAndGet();
                    log.info("并发推进被拒（可接受）：{}", e.getMessage());
                }
            });
        }
        gate.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "并发推进未在 60s 内结束");

        long outputs = outputCount(sale.getId());
        log.info("成功 {} 个，被拒 {} 个，出库单 {} 张，单据状态 = {}",
                success.get(), rejected.get(), outputs, saleService.get(sale.getId()).getStatus());

        assertEquals(1, outputs,
                "并发推进只能生成 1 张出库单，实际 " + outputs + " 张 —— 下游单据重复生成，库存会翻倍");
    }

    @Test
    @DisplayName("顺序重复调用 setBillDetailsAllFinished：幂等，不重复生成出库单")
    public void repeatedSetBillDetailsAllFinishedIsIdempotent() {
        SaleEntity sale = auditedSale(10D);
        saleService.setBillDetailsAllFinished(sale.getId());
        assertEquals(1, outputCount(sale.getId()), "首次推进应生成 1 张出库单");

        // 再调两次，都不应重复生成
        for (int i = 1; i <= 2; i++) {
            try {
                saleService.setBillDetailsAllFinished(sale.getId());
            } catch (Exception e) {
                log.info("第 {} 次重复调用被状态守卫拒绝：{}", i, e.getMessage());
            }
        }
        long outputs = outputCount(sale.getId());
        log.info("重复推进 2 次后，出库单 {} 张", outputs);
        assertEquals(1, outputs, "重复推进不应重复生成出库单，实际 " + outputs + " 张");
    }

    @Test
    @DisplayName("订单手动完结并发调用：入库单只能有 1 张")
    public void concurrentOrderManualFinishGeneratesOneInput() throws Exception {
        var orderService = cn.hamm.spms.module.mes.MesServices.getOrderService();
        // 开启「允许任意状态手动完成订单」开关
        setFlag(ConfigFlag.ORDER_MANUAL_FINISH, true);

        var order = orderService.addAndGet(new cn.hamm.spms.module.mes.order.OrderEntity()
                .setMaterial(materialService.get(1L))
                .setQuantity(10D)
                // type 默认是 1(计划订单)，而本用例不挂计划单
                .setType(cn.hamm.spms.module.mes.order.enums.OrderType.OTHER.getKey()));
        orderService.updateToDatabase(order);
        // 先审核，否则状态守卫会拒绝（审核中 / 生产中 / 暂停中 才允许手动完结）
        var auditing = orderService.get(order.getId());
        orderService.setAudited(auditing);
        orderService.updateToDatabase(auditing);
        // 必须先有报工量，否则 afterAllBillDetailFinished 走「直接完成、无需入库」分支
        orderService.updateToDatabase(orderService.get(order.getId())
                .setFinishQuantity(10D).setNgQuantity(10D));
        log.info("并发手动完结订单 ID = {}，状态 = {}", order.getId(), order.getStatus());

        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        CountDownLatch gate = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    gate.await(5, TimeUnit.SECONDS);
                    orderService.setOrderFinishedManually(order.getId());
                    success.incrementAndGet();
                } catch (Exception e) {
                    rejected.incrementAndGet();
                    log.info("并发手动完结被拒（可接受）：{}", e.getMessage());
                }
            });
        }
        gate.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "并发手动完结未在 60s 内结束");

        long inputs = WmsServices.getInputService().filter(null).stream()
                .filter(i -> i.getOrder() != null && i.getOrder().getId() == order.getId())
                .count();
        log.info("成功 {} 个，被拒 {} 个，入库单 {} 张", success.get(), rejected.get(), inputs);

        assertEquals(1, inputs,
                "并发手动完结只能生成 1 张入库单，实际 " + inputs + " 张 —— 入库量会凭空翻倍");
    }

    @AfterEach
    void restoreFlags() {
        setFlag(ConfigFlag.ORDER_MANUAL_FINISH, false);
    }

    private void setFlag(ConfigFlag flag, boolean value) {
        var configService = SystemServices.getConfigService();
        ConfigEntity config = configService.get(flag);
        configService.updateToDatabase(config.setConfig(value ? "1" : "0"));
    }
}
