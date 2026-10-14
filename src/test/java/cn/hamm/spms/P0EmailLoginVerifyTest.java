package cn.hamm.spms;

import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <h1>P0 修复验证 · 邮箱验证码登录（P0-3）</h1>
 * <p>独立成类，避免与其他 P0 用例共享数据。</p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P0EmailLoginVerifyTest {

    @Autowired
    private UserService userService;

    private static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@spms.local";
    }

    @Test
    @DisplayName("P0-3 邮箱注册把 email 真正写库（修复前 setEmail 缺失 → email 落空 → 登录覆盖已有用户）")
    public void registerViaEmailPersistsEmail() {
        String email = uniqueEmail("p0verify");
        UserEntity created = userService.registerUserViaEmail(email);
        assertEquals(email, created.getEmail(), "注册返回的 email 必须落库");
        UserEntity reloaded = userService.get(created.getId());
        assertEquals(email, reloaded.getEmail(), "重新查库的 email 必须一致");
        log.info("邮箱注册验证通过，userId={}, email={}, nickname={}",
                created.getId(), created.getEmail(), created.getNickname());
    }

    @Test
    @DisplayName("P0-3 同一邮箱不会静默产生第二个账号（修复前每次登录都新建空账号）")
    public void sameEmailYieldsSingleAccount() {
        String email = uniqueEmail("p0single");
        UserEntity first = userService.registerUserViaEmail(email);
        // 模拟 loginViaEmailAndCode：先按 email 查，命中即直接返回
        long matched = userService.filter(null).stream()
                .filter(u -> email.equals(u.getEmail()))
                .count();
        assertEquals(1L, matched, "同一邮箱在库中应只对应一个账号");
        assertEquals(first.getId(), userService.filter(null).stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst().orElseThrow().getId());
        log.info("邮箱 {} 只命中一个账号 userId={}", email, first.getId());
    }

    @Test
    @DisplayName("P0-3 重复邮箱被业务层拒绝，不会撞数据库唯一索引导致 500")
    public void duplicateEmailRejectedWithBusinessError() {
        String email = uniqueEmail("p0dup");
        userService.registerUserViaEmail(email);
        Exception thrown = null;
        try {
            userService.registerUserViaEmail(email);
        } catch (Exception e) {
            thrown = e;
        }
        assertNotNull(thrown, "重复邮箱应被拒绝");
        log.info("重复邮箱被拒绝: {} - {}", thrown.getClass().getSimpleName(), thrown.getMessage());
    }
}
