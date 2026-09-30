package cn.hamm.spms;

import cn.hamm.spms.module.personnel.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import cn.hamm.airpower.redis.RedisHelper;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P1 修复验证 · 邮箱验证码限流（P1-10）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P1EmailRateLimitVerifyTest {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisHelper redisHelper;

    private Method addEmailFailCount;
    private Method getEmailFailKey;
    private Method deleteEmailCode;

    @BeforeEach
    void setUp() throws Exception {
        Class<?> clazz = UserService.class;
        addEmailFailCount = clazz.getDeclaredMethod("addEmailFailCount", String.class, String.class);
        getEmailFailKey = clazz.getDeclaredMethod("getEmailFailKey", String.class, String.class);
        deleteEmailCode = clazz.getDeclaredMethod("deleteEmailCode", String.class);
        addEmailFailCount.setAccessible(true);
        getEmailFailKey.setAccessible(true);
        deleteEmailCode.setAccessible(true);
        assertNotNull(addEmailFailCount);
    }

    /**
     * 反射调用 getEmailCacheCode，拿到验证码在 redis 中的 key
     */
    private String codeKeyOf(String email) throws Exception {
        Method m = UserService.class.getDeclaredMethod("getEmailCacheCode", String.class);
        m.setAccessible(true);
        return (String) m.invoke(userService, email);
    }

    @Test
    @DisplayName("P1-10 失败计数按 (邮箱 + IP) 双键，不同 IP 互不影响")
    public void failCountIsScopedByEmailAndIp() throws Exception {
        String email = "p1-" + UUID.randomUUID() + "@spms.local";
        String keyA = (String) getEmailFailKey.invoke(userService, email, "10.0.0.1");
        String keyB = (String) getEmailFailKey.invoke(userService, email, "10.0.0.2");
        assertFalse(keyA.equals(keyB), "不同 IP 的失败计数必须是不同的 key");

        addEmailFailCount.invoke(userService, email, "10.0.0.1");
        assertNotNull(redisHelper.get(keyA), "该 IP 的计数应写入");
        assertFalse(redisHelper.hasKey(keyB), "另一个 IP 不应被牵连");
        log.info("P1-10 通过：keyA={} 有计数，keyB={} 无计数", keyA, keyB);
    }

    @Test
    @DisplayName("P1-10 达到阈值时不再删除受害者已收到的验证码（旧实现会删，等于远程锁死账号）")
    public void thresholdNoLongerDeletesVictimCode() throws Exception {
        String email = "p1-" + UUID.randomUUID() + "@spms.local";
        String codeKey = codeKeyOf(email);
        // 模拟受害者已经收到了验证码
        redisHelper.set(codeKey, "REALCODE", 300);
        assertNotNull(redisHelper.get(codeKey));

        // 攻击者从另一个 IP 连续输错，直到触发阈值
        for (int i = 0; i < 6; i++) {
            try {
                addEmailFailCount.invoke(userService, email, "6.6.6.6");
            } catch (Exception e) {
                // 达到阈值后按设计抛异常，这里只需要它被抛出来
                log.info("第 {} 次达到阈值：{}", i + 1, e.getCause() == null ? e.getMessage()
                        : e.getCause().getMessage());
                break;
            }
        }
        assertEquals("REALCODE", redisHelper.get(codeKey),
                "阈值触发后验证码必须仍在，受害者才能正常登录");
        log.info("P1-10 通过：阈值触发后验证码仍为 {}", redisHelper.get(codeKey));
    }

    @Test
    @DisplayName("P1-10 达到阈值后同一 IP 被标记，可用于封禁换邮箱继续尝试")
    public void thresholdMarksTheAttackerIp() throws Exception {
        String email = "p1-" + UUID.randomUUID() + "@spms.local";
        String attackerIp = "7.7.7.7";
        String ipFailKey = "email:ip:" + attackerIp + ":fail";

        for (int i = 0; i < 6; i++) {
            try {
                addEmailFailCount.invoke(userService, email, attackerIp);
            } catch (Exception e) {
                break;
            }
        }
        assertTrue(redisHelper.hasKey(ipFailKey), "触发阈值后应记录该 IP 的失败总数");
        log.info("P1-10 通过：攻击者 IP 已被标记，计数 = {}", redisHelper.get(ipFailKey));
    }

    @Test
    @DisplayName("P1-10 发邮件的限流 Key 齐备（邮箱 / IP / 全局 三个维度）")
    public void sendRateLimitKeysExist() throws Exception {
        String ip = "8.8.8.8";
        String ipSendKey = invokeKey("getEmailIpSendKey", ip);
        String globalSendKey = invokeKey("getEmailGlobalSendKey");
        assertEquals("email:ip:" + ip + ":send", ipSendKey);
        assertEquals("email:global:send", globalSendKey);
        assertFalse(ipSendKey.equals(globalSendKey), "IP 维度与全局维度必须是不同的 key");
        log.info("P1-10 通过：IP 限流 key={}，全局限流 key={}", ipSendKey, globalSendKey);
    }

    private String invokeKey(String methodName, String... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = String.class;
        }
        Method m = UserService.class.getDeclaredMethod(methodName, types);
        m.setAccessible(true);
        Object result = m.invoke(userService, (Object[]) args);
        return String.valueOf(result);
    }
}
