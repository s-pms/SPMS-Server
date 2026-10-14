package cn.hamm.spms;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P1 修复验证 · OAuth 回调地址白名单（P1-7）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P1OauthRedirectVerifyTest {

    private Object controller;
    private Method isRedirectUriAllowed;

    private boolean allowed(String requested, String registered) throws Exception {
        return (boolean) isRedirectUriAllowed.invoke(controller, requested, registered);
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        Class<?> clazz = Class.forName("cn.hamm.spms.module.open.oauth.OauthController");
        controller = clazz.getDeclaredConstructor().newInstance();
        isRedirectUriAllowed = clazz.getDeclaredMethod("isRedirectUriAllowed", String.class, String.class);
        isRedirectUriAllowed.setAccessible(true);
        assertNotNull(isRedirectUriAllowed);
    }

    @Test
    @DisplayName("P1-7 攻击场景：redirectUri 指向攻击者站点时被拒绝")
    public void attackerSiteIsRejected() throws Exception {
        assertFalse(allowed("https://evil.com/steal", "https://app.example.com/callback"),
                "跨站回调必须拒绝，否则授权码会被 302 送到攻击者");
        assertFalse(allowed("http://evil.com", "https://app.example.com/callback"));
        log.info("P1-7 通过：攻击者站点被拒绝");
    }

    @Test
    @DisplayName("P1-7 完全一致的注册地址被允许")
    public void exactMatchIsAllowed() throws Exception {
        assertTrue(allowed("https://app.example.com/callback", "https://app.example.com/callback"));
        log.info("P1-7 通过：完全一致被允许");
    }

    @Test
    @DisplayName("P1-7 注册路径下的子路径被允许（正常业务需要）")
    public void subPathUnderRegisteredPathIsAllowed() throws Exception {
        assertTrue(allowed("https://app.example.com/callback/result", "https://app.example.com/callback"));
        assertTrue(allowed("https://app.example.com/callback", "https://app.example.com/callback/"),
                "注册地址带尾斜杠、请求不带，应视为同一路径");
        assertTrue(allowed("https://app.example.com/callback/", "https://app.example.com/callback"));
        log.info("P1-7 通过：子路径被允许");
    }

    @Test
    @DisplayName("P1-7 前缀绕过：/callback-evil 不能匹配 /callback")
    public void prefixBypassIsRejected() throws Exception {
        assertFalse(allowed("https://app.example.com/callback-evil", "https://app.example.com/callback"),
                "字符串前缀匹配会被 /callback-evil 绕过，必须逐段匹配");
        log.info("P1-7 通过：前缀绕过被拒绝");
    }

    @Test
    @DisplayName("P1-7 端口与协议不同均被拒绝")
    public void differentPortOrSchemeIsRejected() throws Exception {
        assertFalse(allowed("https://app.example.com:8443/callback", "https://app.example.com/callback"),
                "端口不同必须拒绝");
        assertFalse(allowed("http://app.example.com/callback", "https://app.example.com/callback"),
                "协议不同必须拒绝（防止降级到明文）");
        log.info("P1-7 通过：端口/协议差异被拒绝");
    }

    @Test
    @DisplayName("P1-7 相对地址、userinfo 注入、非法格式均被拒绝")
    public void malformedUrisAreRejected() throws Exception {
        assertFalse(allowed("/callback", "https://app.example.com/callback"), "相对地址必须拒绝");
        assertFalse(allowed("https://evil.com@app.example.com/callback", "https://app.example.com/callback"),
                "userinfo 段必须拒绝（极易在人工审阅时看错真实主机）");
        assertFalse(allowed("https://app.example.com@evil.com/callback", "https://app.example.com/callback"),
                "userinfo 伪装成注册主机、实际指向攻击者的地址必须拒绝");
        assertFalse(allowed("https://app.example.com/callback", ""), "注册地址为空时必须拒绝");
        assertFalse(allowed("", "https://app.example.com/callback"), "请求地址为空时必须拒绝");
        assertFalse(allowed("https://app.example.com/callback", null), "注册地址为 null 时必须拒绝");
        assertFalse(allowed(null, "https://app.example.com/callback"), "请求地址为 null 时必须拒绝");
        log.info("P1-7 通过：畸形地址均被拒绝");
    }

    @Test
    @DisplayName("P1-7 注册地址为根路径时，站内任意路径都允许")
    public void rootRegisteredPathAllowsAnyPath() throws Exception {
        assertTrue(allowed("https://app.example.com/anything", "https://app.example.com"));
        assertTrue(allowed("https://app.example.com/", "https://app.example.com/"));
        log.info("P1-7 通过：根路径注册允许站内任意路径");
    }
}
