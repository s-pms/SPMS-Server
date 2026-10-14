package cn.hamm.spms;

import cn.hamm.spms.module.asset.contract.ContractEntity;
import cn.hamm.spms.module.asset.contract.ContractService;
import cn.hamm.spms.module.asset.contract.enums.ContractStatus;
import cn.hamm.spms.module.asset.contract.participant.ContractParticipantLinkService;
import cn.hamm.spms.module.asset.contract.participant.ParticipantEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * <h1>P2 · 关联改造验证 · 合同参与方（ManyToMany -> 中间表实体）</h1>
 * <p>
 * 验证用「中间表实体」取代 {@code @ManyToMany} 之后：
 * 写入能落中间表、读取能组装回前端、HTTP 序列化在 open-in-view 关闭时仍正常。
 * 这是本批改造的样板，后续 7 个关联按同一模式推进。
 * </p>
 */
@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P2LinkRefactorVerifyTest {

    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractParticipantLinkService linkService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private cn.hamm.spms.module.personnel.user.UserService userService;

    /**
     * 建合同
     * <p>
     * 显式给 code：自动编号当前存在独立缺陷（每次都生成 CON2026090001，
     * 导致唯一键冲突），与本次关联改造无关，已单独记录。
     * </p>
     */
    private ContractEntity newContract(String name) {
        return new ContractEntity()
                .setCode("C-" + UUID.randomUUID())
                .setName(name)
                .setStatus(ContractStatus.INVALID.getKey());
    }

    private ParticipantEntity participant(String name) {
        return new ParticipantEntity()
                .setName(name)
                .setPhone("13800000000")
                .setEmail(name + "@test.com")
                .setIdentification("110101199001011234")
                .setType(1)
                .setRole(1)
                .setCertificateType(1);
    }

    @Test
    @DisplayName("ManyToMany->中间表：保存合同时参与方写入中间表，读取时能组装回来")
    public void participantsRoundTripThroughLinkTable() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        ParticipantEntity p1 = participant("甲-" + tag);
        ParticipantEntity p2 = participant("乙-" + tag);

        ContractEntity contract = contractService.addAndGet(newContract("中间表合同-" + tag)
                .setParticipantList(new java.util.LinkedHashSet<>(List.of(p1, p2))));

        // 读取时应能组装回 2 个参与方
        ContractEntity loaded = contractService.get(contract.getId());
        assertEquals(2, loaded.getParticipantList().size(),
                "读取时应从中间表组装出参与方，实际 " + loaded.getParticipantList().size());
        assertNotNull(loaded.getParticipantList().iterator().next().getId(),
                "参与方应已落库并拿到 ID");
        log.info("参与方往返通过：保存 2 个，读回 {} 个", loaded.getParticipantList().size());

        // 中间表里确实有 2 条关联
        assertEquals(2, linkService.filter(
                        new cn.hamm.spms.module.asset.contract.participant.ContractParticipantLinkEntity()
                                .setContract(new ContractEntity().setId(contract.getId()))).size(),
                "中间表应落 2 条关联记录");
        log.info("中间表关联记录数 = 2（已验证）");
    }

    @Test
    @DisplayName("ManyToMany->中间表：修改合同时参与方全量覆盖，不会残留旧关联")
    public void participantsAreReplacedOnUpdate() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        ContractEntity contract = contractService.addAndGet(newContract("覆盖合同-" + tag)
                .setParticipantList(new java.util.LinkedHashSet<>(List.of(
                        participant("旧1-" + tag), participant("旧2-" + tag)))));

        // 只保留 1 个
        contractService.update(newContract("覆盖合同改-" + tag)
                .setId(contract.getId())
                .setParticipantList(new java.util.LinkedHashSet<>(List.of(participant("新-" + tag)))));

        ContractEntity loaded = contractService.get(contract.getId());
        assertEquals(1, loaded.getParticipantList().size(),
                "修改后应只剩 1 个参与方，实际 " + loaded.getParticipantList().size()
                        + " -> " + loaded.getParticipantList().stream()
                        .map(ParticipantEntity::getName).toList());
        log.info("全量覆盖通过：改后只剩 {}", loaded.getParticipantList().size());
    }

    @Test
    @DisplayName("ManyToMany->中间表：清空参与方后关联也被删除")
    public void clearingParticipantsRemovesLinks() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        ContractEntity contract = contractService.addAndGet(newContract("清空合同-" + tag)
                .setParticipantList(new java.util.LinkedHashSet<>(List.of(participant("待清-" + tag)))));
        assertEquals(1, contractService.get(contract.getId()).getParticipantList().size());

        contractService.update(newContract("清空合同改-" + tag)
                .setId(contract.getId())
                .setParticipantList(new java.util.LinkedHashSet<>()));

        assertEquals(0, contractService.get(contract.getId()).getParticipantList().size(),
                "清空后不应残留参与方");
        log.info("清空参与方通过，中间表无残留");
    }

    @Test
    @DisplayName("ManyToMany->中间表：open-in-view 关闭时 HTTP 接口仍能返回参与方")
    public void httpStillReturnsParticipants() throws Exception {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        contractService.addAndGet(newContract("HTTP合同-" + tag)
                .setParticipantList(new java.util.LinkedHashSet<>(List.of(participant("HTTP-" + tag)))));

        String token = userService.createAccessToken(1L);
        MvcResult result = mockMvc.perform(post("/contract/getList")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":{\"current\":1,\"size\":50}}"))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertTrue(!body.contains("LazyInitialization") && !body.contains("no Session"),
                "不应出现懒加载异常，实际响应：" + body.substring(0, Math.min(300, body.length())));
        assertTrue(!body.contains("\"code\":500"), "接口不应 500，实际：" + body.substring(0, Math.min(300, body.length())));
        assertTrue(body.contains("HTTP-" + tag), "HTTP 响应中应包含参与方姓名");
        log.info("HTTP 序列化通过：响应 {} 字节，含参与方", body.length());
    }
}
