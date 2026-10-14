package cn.hamm.spms;

import cn.hamm.spms.module.asset.contract.ContractEntity;
import cn.hamm.spms.module.asset.contract.ContractService;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * <h1>P1 修复验证 · 合同状态机（P1-9）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P1ContractVerifyTest {

    @Autowired
    private ContractService contractService;

    private ContractEntity freshContract() {
        return contractService.addAndGet(new ContractEntity()
                .setName("合同-" + UUID.randomUUID().toString().substring(0, 6)));
    }

    @Test
    @DisplayName("P1-9 新建时传入 status 无效，一律落为「未生效」")
    public void addIgnoresClientStatus() {
        ContractEntity contract = contractService.addAndGet(new ContractEntity()
                .setName("直生效-" + UUID.randomUUID().toString().substring(0, 6))
                .setStatus(ContractStatus.EFFECTIVE.getKey()));
        assertEquals(ContractStatus.INVALID.getKey(), contract.getStatus(),
                "新建合同不应由客户端决定状态");
        log.info("P1-9 通过：新建传入 status=生效中，实际落库为 {}", contract.getStatus());
    }

    @Test
    @DisplayName("P1-9 update 直接改状态被忽略（已终止的合同不能被改回生效中）")
    public void updateIgnoresClientStatus() {
        ContractEntity contract = freshContract();
        contractService.enforce(contract.getId());
        contractService.stop(contract.getId());
        assertEquals(ContractStatus.TERMINATED.getKey(),
                contractService.get(contract.getId()).getStatus(), "前置：应已终止");

        // 模拟越权请求体：id + status=生效中
        contractService.update(new ContractEntity()
                .setId(contract.getId())
                .setName("改名-" + UUID.randomUUID().toString().substring(0, 6))
                .setStatus(ContractStatus.EFFECTIVE.getKey()));

        ContractEntity after = contractService.get(contract.getId());
        assertEquals(ContractStatus.TERMINATED.getKey(), after.getStatus(),
                "已终止合同不得被 update 改回生效中");
        log.info("P1-9 通过：update 试图复活已终止合同被忽略，状态仍为 {}", after.getStatus());
    }

    @Test
    @DisplayName("P1-9 update 改其他字段正常生效（守卫没有误伤正常编辑）")
    public void updateOtherFieldsStillWorks() {
        ContractEntity contract = freshContract();
        String newName = "改名成功-" + UUID.randomUUID().toString().substring(0, 6);
        contractService.update(new ContractEntity()
                .setId(contract.getId())
                .setName(newName));
        assertEquals(newName, contractService.get(contract.getId()).getName(),
                "正常编辑不应被状态守卫拦截");
        log.info("P1-9 通过：正常编辑未被误伤");
    }

    @Test
    @DisplayName("P1-9 受控的状态流转不受守卫影响（enforce / stop 仍能改状态）")
    public void controlledTransitionsStillWork() {
        ContractEntity contract = freshContract();
        contractService.enforce(contract.getId());
        assertEquals(ContractStatus.EFFECTIVE.getKey(),
                contractService.get(contract.getId()).getStatus(), "enforce 应生效");

        contractService.stop(contract.getId());
        assertEquals(ContractStatus.TERMINATED.getKey(),
                contractService.get(contract.getId()).getStatus(), "stop 应生效");
        log.info("P1-9 通过：enforce -> 生效中，stop -> 已终止，受控流转正常");
    }

    @Test
    @DisplayName("P1-9 非法流转仍被状态机拒绝（已终止不能再次终止）")
    public void illegalTransitionStillRejected() {
        ContractEntity contract = freshContract();
        // 前置：未生效的合同直接终止应被状态机拒绝
        assertThrows(Exception.class, () -> contractService.stop(contract.getId()),
                "未生效的合同不应能被终止");
        contractService.enforce(contract.getId());
        // 已终止的合同不能再次终止
        contractService.stop(contract.getId());
        Throwable thrown = assertThrows(Exception.class, () -> contractService.stop(contract.getId()),
                "已终止的合同不应能被再次终止");
        log.info("P1-9 边界：重复终止被拒 -> {}", thrown.getMessage());
    }
}
