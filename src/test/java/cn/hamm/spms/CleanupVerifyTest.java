package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.ChannelServices;
import cn.hamm.spms.module.channel.sale.SaleEntity;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailEntity;
import cn.hamm.spms.module.factory.FactoryServices;
import cn.hamm.spms.module.wms.WmsServices;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>收尾项验证</h1>
 * <p>
 * 三个此前遗留的小问题：
 * </p>
 * <ol>
 *   <li>企业微信回调没标免登录，框架默认 {@code login=true} 会 401</li>
 *   <li>{@code ServiceCron} 每 5 秒打一条测试日志</li>
 *   <li>报工的���态守卫顺序反了，已完成的单据报「未审核」</li>
 * </ol>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class CleanupVerifyTest {

    @Autowired
    private SaleService saleService;
    @Autowired
    private MaterialService materialService;

    /**
     * 建一张已审核的销售单
     *
     * @return 销售单
     */
    private SaleEntity auditedSale() {
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
                .setDetails(List.of(new SaleDetailEntity()
                        .setMaterial(material).setQuantity(10D).setPrice(100D))));
        var auditing = saleService.get(sale.getId());
        saleService.setAudited(auditing);
        saleService.updateToDatabase(auditing);
        return saleService.get(sale.getId());
    }

    @Test
    @DisplayName("P3-18 企业微信回调免登录：匿名调用不返回 401")
    public void wecomCallbackIsPubliclyAccessible() throws Exception {
        var permission = cn.hamm.spms.module.system.WecomController.class
                .getAnnotation(cn.hamm.airpower.curd.permission.Permission.class);
        assertTrue(permission != null && !permission.login(),
                "企业微信回调是服务端调用、带不了 Cookie，必须标 @Permission(login = false)，"
                        + "否则框架默认 login=true 会让它 401");
        log.info("WecomController 已声明免登录");
    }

    @Test
    @DisplayName("P3-1 ServiceCron 测试定时任务已移除")
    public void testCronJobIsRemoved() {
        try {
            Class.forName("cn.hamm.spms.common.cron.ServiceCron");
            throw new AssertionError("ServiceCron 仍存在：它每 5 秒打一条「测试定时任务」日志，"
                    + "生产环境一天约 12 万条无意义日志");
        } catch (ClassNotFoundException e) {
            log.info("ServiceCron 已移除，不再产生测试日志");
        }
    }

    @Test
    @DisplayName("P3-3 已完成的单据再报工，报错应为「已完成」而非「未审核」")
    public void finishedBillReportsFinishedNotUnaudited() {
        SaleEntity sale = auditedSale();
        var detail = ChannelServices.getSaleDetailService().getAllByBillId(sale.getId()).get(0);

        // 先把单据推到已完成
        saleService.addDetailFinishQuantity(
                new SaleDetailEntity().setId(detail.getId()).setBillId(sale.getId()).setQuantity(10D));
        Integer finishedStatus = saleService.get(sale.getId()).getStatus();
        log.info("单据 {} 已推到状态 {}", sale.getId(), finishedStatus);

        // 再报工，应当报「已完成」
        Exception thrown = assertThrows(Exception.class, () -> saleService.addDetailFinishQuantity(
                new SaleDetailEntity().setId(detail.getId()).setBillId(sale.getId()).setQuantity(1D)));
        log.info("已完成单据再报工的报错：{}", thrown.getMessage());

        assertTrue(thrown.getMessage().contains("已完成"),
                "已完成的单据应报「单据已完成」，实际报的是：" + thrown.getMessage()
                        + "（守卫顺序反了会报「未审核」，与实际状态不符）");
        assertTrue(!thrown.getMessage().contains("未审核"),
                "不该报「未审核」：单据实际状态是已完成，报错文案会误导排查");
    }

    @Test
    @DisplayName("P3-3 未审核的单据报工，仍应报「未审核」")
    public void unauditedBillReportsUnaudited() {
        MaterialEntity material = materialService.get(1L);
        var storage = FactoryServices.getStorageService().filter(null).get(0);
        SaleEntity sale = saleService.addAndGet(new SaleEntity()
                .setCustomer(ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(storage)
                .setDetails(List.of(new SaleDetailEntity()
                        .setMaterial(material).setQuantity(10D).setPrice(100D))));
        var detail = ChannelServices.getSaleDetailService().getAllByBillId(sale.getId()).get(0);

        Exception thrown = assertThrows(Exception.class, () -> saleService.addDetailFinishQuantity(
                new SaleDetailEntity().setId(detail.getId()).setBillId(sale.getId()).setQuantity(1D)));
        log.info("未审核单据报工的报错：{}", thrown.getMessage());

        assertTrue(thrown.getMessage().contains("未审核"),
                "未审核的单据应报「单据未审核」，实际：" + thrown.getMessage());
        assertDoesNotThrow(() -> log.info("校验完成"));
    }
}
