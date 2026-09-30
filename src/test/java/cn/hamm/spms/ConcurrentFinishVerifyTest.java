package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.ChannelServices;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.wms.WmsServices;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>并发专项验证 · P2-4</h1>
 * <p>
 * 复现并验证「并发报工导致单据永久卡死」：两个线程同时对<b>同一张单据的不同明细</b>
 * 报工，各自只锁自己那一行，双方快照都早于对方提交，于是都判「明细未全部完成」，
 * 谁都不去推进单据状态。
 * </p>
 * <p>
 * 修复前 8 种单据里只有订单有手动完结接口可救，其余 7 种无自愈手段。
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class ConcurrentFinishVerifyTest {

    @Autowired
    private cn.hamm.spms.base.bill.NewTransactionHelper newTransactionHelper;
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
    private SaleEntity twoDetailAuditedSale(double eachQuantity) {
        MaterialEntity material = materialService.get(1L);
        var storage = cn.hamm.spms.module.factory.FactoryServices.getStorageService().filter(null).get(0);
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

    @Test
    @DisplayName("P2-4 两个线程同时对不同明细报工，单据必须推进到完成态（不能永久卡死）")
    public void concurrentFinishMustNotStuck() throws Exception {
        SaleEntity sale = twoDetailAuditedSale(10D);
        List<SaleDetailEntity> details = ChannelServices.getSaleDetailService().getAllByBillId(sale.getId());
        assertEquals(2, details.size(), "前置：应有 2 行明细");
        log.info("并发测试单据 ID = {}，初始状态 = {}，两行明细 ID = {} / {}",
                sale.getId(), sale.getStatus(), details.get(0).getId(), details.get(1).getId());

        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // 关键：两个线程各对**不同的明细行**报工，这才是 P2-4 描述的场景
        for (SaleDetailEntity detail : details) {
            pool.submit(() -> {
                try {
                    startGate.await(5, TimeUnit.SECONDS);
                    saleService.addDetailFinishQuantity(new SaleDetailEntity()
                            .setId(detail.getId()).setQuantity(10D));
                    success.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                    log.info("线程报错: {}", e.getMessage());
                }
            });
        }
        startGate.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "并发报工未在 60s 内结束");
        log.info("并发报工结果：成功 {} 个线程，失败 {} 个线程", success.get(), failed.get());

        List<SaleDetailEntity> after = ChannelServices.getSaleDetailService().getAllByBillId(sale.getId());
        boolean allFinished = after.stream().allMatch(d -> Boolean.TRUE.equals(d.getIsFinished()));
        Integer finalStatus = saleService.get(sale.getId()).getStatus();
        log.info("并发后：明细完成标记 = {}，单据状态 = {}",
                after.stream().map(d -> String.valueOf(d.getIsFinished())).toList(), finalStatus);

        assertTrue(allFinished, "两个线程各完成一行，明细应全部完成");
        // P2-4 的核心：单据必须被推进。修复前双方快照都早于对方提交，
        // 都判 allMatch=false，谁都不调 setBillDetailsAllFinished，单据永久卡住
        assertNotEquals(cn.hamm.spms.module.channel.sale.enums.SaleStatus.OUTPUTTING.getKey(), finalStatus,
                "单据卡在「出库中」未被推进，这正是 P2-4 描述的永久卡死");

        // 注意：并发测试的外层事务尚未提交（报工在 transactionHelper.run 内），
        // 必须用独立事务查询才能看到已提交的出库单
        Long outputCount = newTransactionHelper.run(() ->
                WmsServices.getOutputService().filter(null).stream()
                        .filter(o -> o.getSale() != null && o.getSale().getId().equals(sale.getId()))
                        .count());
        log.info("生成的出库单数 = {}", outputCount);
        assertEquals(1, outputCount, "应且仅应生成 1 张出库单（0 张=卡死，多张=重复）");
    }
}
