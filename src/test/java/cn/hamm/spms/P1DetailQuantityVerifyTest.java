package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P1 修复验证 · 单据明细数量（P1-1 / P1-6）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P1DetailQuantityVerifyTest {

    @Autowired
    private MaterialService materialService;
    @Autowired
    private SaleService saleService;

    private MaterialEntity material() {
        return materialService.get(1L);
    }

    private cn.hamm.spms.module.factory.storage.StorageEntity storage() {
        return cn.hamm.spms.module.factory.FactoryServices.getStorageService().get(1L);
    }

    /**
     * 确保物料在该仓库有库存记录（P0-5 生成出库单时需要）
     */
    private void ensureInventory() {
        var inventoryService = cn.hamm.spms.module.wms.WmsServices.getInventoryService();
        var exist = inventoryService.getByMaterialIdAndStorageId(material().getId(), storage().getId());
        if (exist != null) {
            return;
        }
        inventoryService.addAndGet(new cn.hamm.spms.module.wms.inventory.InventoryEntity()
                .setMaterial(material())
                .setStorage(storage())
                .setType(cn.hamm.spms.module.wms.inventory.enums.InventoryType.STORAGE.getKey())
                .setQuantity(1000D));
    }

    /**
     * 建一张带两条相同物料明细的销售单，均已审核
     */
    private SaleEntity saleWithTwoSameMaterialRows(double eachQuantity) {
        ensureInventory();
        MaterialEntity m = material();
        return saleService.addAndGet(new SaleEntity()
                .setStatus(SaleStatus.AUDITING.getKey())
                .setCustomer(cn.hamm.spms.module.channel.ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(storage())
                .setDetails(List.of(
                        new SaleDetailEntity().setMaterial(m).setQuantity(eachQuantity).setPrice(100D),
                        new SaleDetailEntity().setMaterial(m).setQuantity(eachQuantity).setPrice(100D))));
    }

    private List<SaleDetailEntity> detailsOf(SaleEntity sale) {
        return cn.hamm.spms.module.channel.ChannelServices.getSaleDetailService().getAllByBillId(sale.getId());
    }

    @Test
    @DisplayName("P1-1 同一单据两行相同物料，出库 1 件只能记 1 件（修复前两行都记 1 件 → 超量发货）")
    public void quantityIsDistributedNotDuplicated() {
        SaleEntity sale = saleWithTwoSameMaterialRows(10D);
        log.info("分配前单据已完成标记 = {}", saleService.get(sale.getId()).getStatus());

        // 触发 updateDetailQuantity：出库 1 件
        cn.hamm.spms.module.channel.ChannelServices.getSaleDetailService().updateDetailQuantity(
                sale.getId(), 1D, saleService, detail -> {
                });

        List<SaleDetailEntity> details = detailsOf(sale);
        double total = details.stream().mapToDouble(d ->
                d.getFinishQuantity() == null ? 0D : d.getFinishQuantity()).sum();
        log.info("分配后各行完成量 = {}", details.stream()
                .map(d -> String.valueOf(d.getFinishQuantity())).toList());
        assertEquals(1D, total, 0.0001D, "本次分配的 1 件不应在多行之间重复计入");
    }

    @Test
    @DisplayName("P1-1 多次分配必须累加（修复前是覆盖，生产计划完成量永远只显示最后一次）")
    public void quantityAccumulatesAcrossCalls() {
        SaleEntity sale = saleWithTwoSameMaterialRows(10D);
        var detailService = cn.hamm.spms.module.channel.ChannelServices.getSaleDetailService();

        detailService.updateDetailQuantity(sale.getId(), 3D, saleService, d -> {
        });
        double after1 = sumFinish(detailsOf(sale));
        detailService.updateDetailQuantity(sale.getId(), 4D, saleService, d -> {
        });
        double after2 = sumFinish(detailsOf(sale));

        log.info("第一次 3 件后累计 = {}，第二次 4 件后累计 = {}", after1, after2);
        assertEquals(3D, after1, 0.0001D);
        assertEquals(7D, after2, 0.0001D, "第二次分配后应累加为 7，而不是被覆盖成 4");
    }

    @Test
    @DisplayName("P1-1 累计超过预期即自动完成（修复前不会标记完成）")
    public void autoFinishWhenAccumulated() {
        SaleEntity sale = saleWithTwoSameMaterialRows(10D);
        var detailService = cn.hamm.spms.module.channel.ChannelServices.getSaleDetailService();

        // 必须先审核：P2-1 修复后 setBillDetailsAllFinished 有了状态守卫，
        // 「审核中」的单据不再能被直接推到完成态。修复前这里不审核也能过，
        // 恰恰说明状态机是形同虚设的
        SaleEntity auditing = saleService.get(sale.getId());
        saleService.setAudited(auditing);
        saleService.updateToDatabase(auditing);
        log.info("审核后单据状态 = {}", saleService.get(sale.getId()).getStatus());

        detailService.updateDetailQuantity(sale.getId(), 10D, saleService, d -> {
        });
        detailService.updateDetailQuantity(sale.getId(), 10D, saleService, d -> {
        });
        List<SaleDetailEntity> details = detailsOf(sale);
        double total = sumFinish(details);
        log.info("累计 {} 件，各行完成标记 = {}", total,
                details.stream().map(d -> String.valueOf(d.getIsFinished())).toList());
        assertEquals(20D, total, 0.0001D, "两行各 10 件都应记满");
        assertTrue(details.stream().allMatch(d -> Boolean.TRUE.equals(d.getIsFinished())),
                "累计达预期后应自动标记完成");
    }

    @Test
    @DisplayName("P1-1 负数分配被拒绝（修复前会 break 掉循环静默无效）")
    public void negativeQuantityRejected() {
        SaleEntity sale = saleWithTwoSameMaterialRows(10D);
        var detailService = cn.hamm.spms.module.channel.ChannelServices.getSaleDetailService();
        Throwable thrown = assertThrows(Exception.class,
                () -> detailService.updateDetailQuantity(sale.getId(), -5D, saleService, d -> {
                }));
        log.info("P1-1 负数被拒绝：{}", thrown.getMessage());
        assertNotNull(thrown);
    }

    @Test
    @DisplayName("P1-6 单据报工数量不允许为负（防止把已完成数量「修回来」污染库存和金额）")
    public void billNegativeQuantityRejected() {
        // 走 addDetailFinishQuantity 这条路径
        ensureInventory();
        MaterialEntity m = material();
        SaleEntity sale = saleService.addAndGet(new SaleEntity()
                .setStatus(SaleStatus.AUDITING.getKey())
                .setCustomer(cn.hamm.spms.module.channel.ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(storage())
                .setDetails(List.of(new SaleDetailEntity().setMaterial(m).setQuantity(10D).setPrice(10D))));
        SaleEntity audited = saleService.get(sale.getId());
        saleService.setAudited(audited);
        saleService.updateToDatabase(audited);

        SaleDetailEntity detail = detailsOf(sale).get(0);
        Throwable thrown = assertThrows(Exception.class, () -> saleService.addDetailFinishQuantity(
                new SaleDetailEntity().setId(detail.getId()).setQuantity(-3D)));
        log.info("P1-6 负数被拒绝：{}", thrown.getMessage());
        assertNotNull(thrown);
    }

    @Test
    @DisplayName("P1-6 单据允许超过计划数量（按你的要求不设上限）")
    public void billQuantityMayExceedPlan() {
        ensureInventory();
        MaterialEntity m = material();
        SaleEntity sale = saleService.addAndGet(new SaleEntity()
                .setStatus(SaleStatus.AUDITING.getKey())
                .setCustomer(cn.hamm.spms.module.channel.ChannelServices.getCustomerService().filter(null).get(0))
                .setStorage(storage())
                .setDetails(List.of(new SaleDetailEntity().setMaterial(m).setQuantity(10D).setPrice(10D))));
        SaleEntity audited = saleService.get(sale.getId());
        saleService.setAudited(audited);
        saleService.updateToDatabase(audited);

        SaleDetailEntity detail = detailsOf(sale).get(0);
        // 计划 10 件，报 25 件：明细层允许
        saleService.addDetailFinishQuantity(
                new SaleDetailEntity().setId(detail.getId()).setQuantity(25D));
        SaleDetailEntity reloaded = detailsOf(sale).get(0);
        log.info("计划 10 件报了 25 件，实际完成量 = {}", reloaded.getFinishQuantity());
        assertEquals(25D, reloaded.getFinishQuantity(), 0.0001D, "按要求不设上限，应原样累加");
    }

    private double sumFinish(List<SaleDetailEntity> details) {
        return details.stream().mapToDouble(d -> d.getFinishQuantity() == null ? 0D : d.getFinishQuantity()).sum();
    }
}
