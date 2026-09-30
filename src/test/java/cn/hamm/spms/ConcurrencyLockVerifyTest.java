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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>并发与锁专项验证</h1>
 * <p>
 * 三个问题：
 * <ol>
 *   <li><b>锁泄漏</b>：单据推进后，MySQL 里残留「空闲但未提交、仍持有行锁」的事务</li>
 *   <li><b>编码回滚</b>：事务回滚把编码规则的 current_sn 一起回滚，导致编码重复</li>
 *   <li><b>并发卡死</b>（P2-4）：两个报工打在同一单据的不同明细，双方都判「未全部完成」</li>
 * </ol>
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class ConcurrencyLockVerifyTest {

    @Autowired
    private SaleService saleService;
    @Autowired
    private MaterialService materialService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 统计「空闲但仍未提交、且持有行锁」的事务数
     * <p>
     * 空闲事务的特征：{@code trx_state=RUNNING} 但 {@code trx_query IS NULL}
     * （没在执行任何 SQL），同时 {@code trx_rows_locked > 0}。
     * </p>
     *
     * @return 泄漏事务数
     */
    private int leakedTransactions() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.innodb_trx "
                        + "where trx_query is null and trx_rows_locked > 0", Integer.class);
        return count == null ? 0 : count;
    }

    private SaleEntity freshAuditedSale(double quantity) {
        MaterialEntity material = materialService.get(1L);
        var storage = cn.hamm.spms.module.factory.FactoryServices.getStorageService().filter(null).get(0);
        var inv = cn.hamm.spms.module.wms.WmsServices.getInventoryService();
        if (inv.getByMaterialIdAndStorageId(material.getId(), storage.getId()) == null) {
            inv.addAndGet(new cn.hamm.spms.module.wms.inventory.InventoryEntity()
                    .setMaterial(material).setStorage(storage)
                    .setType(cn.hamm.spms.module.wms.inventory.enums.InventoryType.STORAGE.getKey())
                    .setQuantity(10000D));
        }
        SaleEntity sale = saleService.addAndGet(new SaleEntity()
                .setCustomer(ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(storage)
                .setDetails(List.of(new SaleDetailEntity()
                        .setMaterial(material).setQuantity(quantity).setPrice(100D))));
        SaleEntity auditing = saleService.get(sale.getId());
        saleService.setAudited(auditing);
        saleService.updateToDatabase(auditing);
        return saleService.get(sale.getId());
    }

    @Test
    @DisplayName("并发1：单据推进后不应留下空闲持锁的事务（锁泄漏）")
    public void noLeakedTransactionAfterBillProgress() {
        int before = leakedTransactions();
        log.info("推进前泄漏事务数 = {}", before);

        SaleEntity sale = freshAuditedSale(10D);
        var detail = ChannelServices.getSaleDetailService().getAllByBillId(sale.getId()).get(0);
        ChannelServices.getSaleDetailService().updateDetailQuantity(sale.getId(), 10D, saleService, d -> {
        });

        int after = leakedTransactions();
        log.info("推进后泄漏事务数 = {}", after);
        assertEquals(before, after,
                "单据推进后残留了持锁的空闲事务：生产环境会持续占用连接、最终打满 Hikari 池");
    }

    @Test
    @DisplayName("并发2：连续建多台设备，编码不重复（验证 current_sn 是否被回滚）")
    public void codeSequenceNotRolledBack() {
        var codeRuleService = cn.hamm.spms.module.system.SystemServices.getCodeRuleService();
        var field = cn.hamm.spms.module.system.coderule.enums.CodeRuleField.DeviceCode;
        var ruleBefore = codeRuleService.getByRuleField(field.getKey());
        int snBefore = ruleBefore.getCurrentSn();
        log.info("建单前 currentSn = {}", snBefore);

        // DeviceEntity 无必填关联，是最干净的编码递增测试载体
        var deviceService = cn.hamm.spms.module.asset.AssetServices.getDeviceService();
        for (int i = 0; i < 3; i++) {
            var device = deviceService.addAndGet(new cn.hamm.spms.module.asset.device.DeviceEntity()
                    .setName("编码诊断-" + UUID.randomUUID().toString().substring(0, 6)));
            assertTrue(device.getCode() != null && !device.getCode().isBlank(),
                    "第 " + (i + 1) + " 台设备未生成编码");
            log.info("第 {} 台设备编码 = {}", i + 1, device.getCode());
        }
        var ruleAfter = codeRuleService.getByRuleField(field.getKey());
        log.info("建 3 台后 currentSn = {}（建单前 {}）", ruleAfter.getCurrentSn(), snBefore);
        assertTrue(ruleAfter.getCurrentSn() >= snBefore + 3,
                "编码流水号应递增 3，实际 " + snBefore + " -> " + ruleAfter.getCurrentSn()
                        + "（递增不足说明事务被回滚，编码会重复）");
    }
}
