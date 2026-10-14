package cn.hamm.spms;

import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.factory.FactoryServices;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.wms.inventory.InventoryEntity;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.inventory.enums.InventoryType;
import cn.hamm.spms.module.wms.move.MoveEntity;
import cn.hamm.spms.module.wms.move.detail.MoveDetailEntity;
import cn.hamm.spms.module.wms.move.enums.MoveStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P1 修复验证 · 移库记账单据（P1-5）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P1MoveBillVerifyTest {

    @Autowired
    private MaterialService materialService;
    @Autowired
    private InventoryService inventoryService;

    private MaterialEntity material() {
        return materialService.get(1L);
    }

    private StorageEntity freshStorage(String tag) {
        return FactoryServices.getStorageService()
                .addAndGet(new StorageEntity().setName("移库测试仓-" + tag));
    }

    private InventoryEntity ensureInventory(StorageEntity storage, double quantity) {
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

    private void setFlag(ConfigFlag flag, boolean value) {
        var configService = SystemServices.getConfigService();
        ConfigEntity config = configService.get(flag);
        configService.updateToDatabase(config.setConfig(value ? "1" : "0"));
    }

    @AfterEach
    void restoreFlags() {
        setFlag(ConfigFlag.INPUT_BILL_AUTO_AUDIT, false);
        setFlag(ConfigFlag.OUTPUT_BILL_AUTO_AUDIT, false);
    }

    @Test
    @DisplayName("P1-5 打开入/出库自动审核后，移库单完成仍能正常生成记账单据（修复前整体回滚）")
    public void moveWorksWithAutoAuditEnabled() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        StorageEntity from = freshStorage(tag + "-源");
        StorageEntity to = freshStorage(tag + "-目");
        InventoryEntity source = ensureInventory(from, 50D);

        // 打开自动审核 —— 这正是让移库整体不可用的开关
        setFlag(ConfigFlag.INPUT_BILL_AUTO_AUDIT, true);
        setFlag(ConfigFlag.OUTPUT_BILL_AUTO_AUDIT, true);
        log.info("已开启 入库单自动审核 / 出库单自动审核");

        var moveService = WmsServices.getMoveService();
        MoveEntity move = moveService.addAndGet(new MoveEntity()
                .setStatus(MoveStatus.AUDITING.getKey())
                .setStorage(to)
                .setDetails(List.of(new MoveDetailEntity()
                        .setInventory(source)
                        .setQuantity(10D))));
        MoveEntity audited = moveService.get(move.getId());
        moveService.setAudited(audited);
        moveService.updateToDatabase(audited);

        var detailService = WmsServices.getMoveDetailService();
        MoveDetailEntity detail = detailService.getAllByBillId(move.getId()).get(0);

        assertDoesNotThrow(() -> moveService.addDetailFinishQuantity(
                        new MoveDetailEntity().setId(detail.getId()).setQuantity(10D)),
                "移库报工不应回滚");

        // 记账单据应已生成，且状态为「已完成」
        List<cn.hamm.spms.module.wms.input.InputEntity> inputs =
                WmsServices.getInputService().filter(null).stream()
                        .filter(i -> i.getMove() != null && i.getMove().getId().equals(move.getId()))
                        .toList();
        List<cn.hamm.spms.module.wms.output.OutputEntity> outputs =
                WmsServices.getOutputService().filter(null).stream()
                        .filter(o -> o.getMove() != null && o.getMove().getId().equals(move.getId()))
                        .toList();
        assertEquals(1, inputs.size(), "应生成 1 张入库记账单");
        assertEquals(1, outputs.size(), "应生成 1 张出库记账单");
        assertEquals(cn.hamm.spms.module.wms.input.enums.InputStatus.DONE.getKey(),
                inputs.get(0).getStatus(), "记账入库单应为已完成");
        assertEquals(cn.hamm.spms.module.wms.output.enums.OutputStatus.DONE.getKey(),
                outputs.get(0).getStatus(), "记账出库单应为已完成");

        // 明细也要真的落库了（改用 addToDatabase 后需手工保存）
        assertEquals(1, WmsServices.getInputDetailService()
                .getAllByBillId(inputs.get(0).getId()).size(), "入库记账单应有明细");
        assertEquals(1, WmsServices.getOutputDetailService()
                .getAllByBillId(outputs.get(0).getId()).size(), "出库记账单应有明细");

        // 库存应真的搬了家
        assertEquals(40D, inventoryService.get(source.getId()).getQuantity(), 0.0001D, "源仓应扣减 10");
        assertNotNull(inventoryService.getByMaterialIdAndStorageId(material().getId(), to.getId()),
                "目标仓应已建库存行");
        log.info("P1-5 通过：移库 {} 正常生成入库 {} 出库 {}，源仓 50 -> 40",
                move.getId(), inputs.get(0).getId(), outputs.get(0).getId());
    }

    @Test
    @DisplayName("P1-5 明细无库存行时给出明确业务异常")
    public void moveDetailWithoutInventoryIsRejected() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        StorageEntity to = freshStorage(tag + "-目");
        var moveService = WmsServices.getMoveService();
        MoveEntity move = moveService.addAndGet(new MoveEntity()
                .setStatus(MoveStatus.AUDITING.getKey())
                .setStorage(to)
                .setDetails(List.of(new MoveDetailEntity().setQuantity(1D))));
        MoveEntity audited = moveService.get(move.getId());
        moveService.setAudited(audited);
        moveService.updateToDatabase(audited);

        var detail = WmsServices.getMoveDetailService().getAllByBillId(move.getId()).get(0);
        Throwable thrown = org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> moveService.addDetailFinishQuantity(
                        new MoveDetailEntity().setId(detail.getId()).setQuantity(1D)));
        log.info("P1-5 边界：{}", thrown.getMessage());
        assertTrue(thrown instanceof java.lang.NullPointerException == false, "不应抛 NPE");
    }
}
