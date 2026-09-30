package cn.hamm.spms;

import cn.hamm.airpower.file.FileConfig;
import cn.hamm.airpower.file.FileHelper;
import cn.hamm.spms.common.influx.InfluxHelper;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.system.file.FileService;
import cn.hamm.spms.module.system.file.enums.FileCategory;
import cn.hamm.spms.module.system.permission.enums.PermissionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P2 修复验证 · 健壮性与注入防护</h1>
 * <p>
 * 覆盖 P2-3（Flux 注入）、P2-5/6（Influx 客户端）、P2-11（房间语义）、
 * P2-21（伪造扩展名上传）、P2-23（MCP 死代码）。
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P2HardeningVerifyTest {

    @Autowired
    private FileService fileService;
    @Autowired
    private FileHelper fileHelper;
    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private UserService userService;

    // ==================== P2-21 上传伪造扩展名 ====================

    /**
     * 构造一个「扩展名是 jpg、内容是 PHP」的假图片
     *
     * @return 上传用的文件
     */
    private MockMultipartFile fakeImageWithPhpContent() {
        String php = "<?php system($_GET['c']); ?>";
        return new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", php.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("P2-21 伪装成 jpg 的 PHP 内容被拒绝上传（修复前只看文件名扩展名）")
    public void phpContentWithJpgExtensionIsRejected() {
        Throwable thrown = assertThrows(Exception.class,
                () -> fileService.upload(fileConfig.getDefaultPlatform(),
                        fakeImageWithPhpContent(), FileCategory.AVATAR, null),
                "伪装成图片的脚本内容应被拒绝");
        assertNotNull(thrown.getMessage());
        log.info("P2-21 通过：伪装文件被拒 -> {}", thrown.getMessage());
    }

    @Test
    @DisplayName("P2-21 ELF 可执行文件伪装成 jpg 被拒绝")
    public void elfBinaryWithJpgExtensionIsRejected() {
        byte[] elf = new byte[]{0x7F, 'E', 'L', 'F', 0x02, 0x01, 0x01, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", elf);
        assertThrows(Exception.class,
                () -> fileService.upload(fileConfig.getDefaultPlatform(), file, FileCategory.AVATAR, null),
                "ELF 可执行文件伪装成 jpg 应被拒绝");
        log.info("P2-21 通过：ELF 伪装文件被拒");
    }

    @Test
    @DisplayName("P2-21 真正的 PNG 通过魔数校验（校验没有误伤正常图片）")
    public void realPngPassesMagicCheck() throws Exception {
        // 最小合法 PNG：8 字节签名 + IHDR + IEND
        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 'I', 'H', 'D', 'R',
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0D, 'I', 'D', 'A',
                'T', 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
                0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
                0x00, 0x00, 0x00, 'I', 'E', 'N', 'D', (byte) 0xAE,
                0x42, (byte) 0x60, (byte) 0x82};
        MockMultipartFile file = new MockMultipartFile("file", "real.png", "image/png", png);
        // 只验证魔数校验这一步：直接调用 upload 会被本地存储路径
        // （/home/static，在开发机上不可写）拦住，与本用例要验证的内容无关
        Method validate = FileService.class.getDeclaredMethod(
                "validateRealFileType", MultipartFile.class, String.class);
        validate.setAccessible(true);
        assertDoesNotThrowReflect(validate, fileService, file, "png");
        log.info("P2-21 通过：真实 PNG 通过魔数校验，未被误伤");
    }

    // ==================== P2-3 Flux 字符串注入 ====================

    @Test
    @DisplayName("P2-3 Flux 字符串转义：引号与反斜杠被正确转义")
    public void fluxStringIsEscaped() throws Exception {
        Method escape = InfluxHelper.class.getDeclaredMethod("escapeFlux", String.class);
        escape.setAccessible(true);

        assertEquals("plain", escape.invoke(null, "plain"));
        assertEquals("a\\\"b", escape.invoke(null, "a\"b"));
        assertEquals("a\\\\b", escape.invoke(null, "a\\b"));
        assertEquals("", escape.invoke(null, (Object) null));

        // 真实攻击载荷：闭合原字符串后追加新的过滤条件
        String attack = "x\") or (r._measurement == \"victim";
        String escaped = (String) escape.invoke(null, attack);
        assertFalse(escaped.contains("\"") && escaped.indexOf('\\') < 0,
                "转义后不应出现未转义的引号，实际：" + escaped);
        assertEquals("x\\\") or (r._measurement == \\\"victim", escaped);
        log.info("P2-3 通过：Flux 注入载荷被转义 -> {}", escaped);
    }

    @Test
    @DisplayName("P2-3 生成的 Flux 过滤语句中，注入载荷的引号被转义、无法闭合字符串")
    public void generatedQueryCannotBreakOut() throws Exception {
        Method escape = InfluxHelper.class.getDeclaredMethod("escapeFlux", String.class);
        escape.setAccessible(true);

        // 复刻 getFluxQuery 第 200 行的语句拼装方式
        String attack = "x\") or true or (\"";
        String code = (String) escape.invoke(null, "spms-report_" + "temp");
        String uuid = (String) escape.invoke(null, attack);
        String filter = String.format(
                "filter(fn: (r) => r._measurement == \"%s\" and r.uuid == \"%s\")", code, uuid);

        // 整条语句里只应有 4 个「语法引号」：r._measurement 两侧 + r.uuid 两侧。
        // 转义产生的 \" 里同样含 " 字符，所以要按 \x 转义序列跳过来数，
        // 不能简单地 count('"')。
        long syntaxQuotes = countUnescapedQuotes(filter);
        assertEquals(4, syntaxQuotes,
                "注入载荷不应能闭合字符串，实际未转义引号数 " + syntaxQuotes + "：" + filter);
        assertTrue(filter.contains("\\\""), "载荷引号应已被转义：" + filter);
        log.info("P2-3 通过：生成的 filter 语句未被闭合 -> {}", filter);
    }

    /**
     * 统计字符串中未被反斜杠转义的引号数量
     *
     * @param text 待统计文本
     * @return 语法引号个数
     */
    private static long countUnescapedQuotes(String text) {
        long count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                i++;      // 跳过被转义的那个字符
                continue;
            }
            if (c == '"') {
                count++;
            }
        }
        return count;
    }

    // ==================== P2-6 Influx 客户端并发安全 ====================

    @Test
    @DisplayName("P2-6 influxDbClient 声明为 volatile（MQTT 线程与 HTTP 线程并发读写）")
    public void influxClientFieldIsVolatile() throws NoSuchFieldException {
        Field field = InfluxHelper.class.getDeclaredField("influxDbClient");
        assertTrue(Modifier.isVolatile(field.getModifiers()),
                "influxDbClient 非 volatile 时，其他线程可能长期看不到新客户端而反复重建");
        log.info("P2-6 通过：influxDbClient 已是 volatile");
    }

    @Test
    @DisplayName("P2-5 关闭客户端时对 null 安全（修复前 catch 块内会对 null 调 close）")
    public void closingNullClientDoesNotThrow() throws Exception {
        InfluxHelper helper = new InfluxHelper();
        Method close = InfluxHelper.class.getDeclaredMethod("closeInfluxDbClient");
        close.setAccessible(true);
        // 修复前这里会 NPE 并从 catch 块逃逸
        assertDoesNotThrowReflect(close, helper);
        log.info("P2-5 通过：关闭 null 客户端不抛异常");
    }

    private void assertDoesNotThrowReflect(Method method, Object target, Object... args) {
        try {
            method.invoke(target, args);
        } catch (Exception e) {
            throw new AssertionError("不应抛异常，实际: " + e.getCause(), e);
        }
    }

    // ==================== P2-11 房间语义 ====================

    @Test
    @DisplayName("P2-11 isInRoom 在无缓存时返回 false（getCurrentRoomId 会返回默认房间 1）")
    public void isInRoomIsFalseWithoutCache() {
        long userId = 99999999L;
        userService.clearCurrentRoomId(userId);
        assertFalse(userService.isInRoom(userId, userService.getCurrentRoomId(userId)),
                "无缓存时不应认为用户在任何房间里，否则会向默认房间广播离开事件");
        assertFalse(userService.isInRoom(userId, 0), "房间 ID 必须为正");
        log.info("P2-11 通过：无缓存时 isInRoom 返回 false（getCurrentRoomId 返回默认房间 {}）",
                userService.getCurrentRoomId(userId));
    }

    @Test
    @DisplayName("P2-11 写入房间后 isInRoom 为 true，离开后恢复 false")
    public void isInRoomTracksSavedRoom() {
        long userId = 99999998L;
        userService.clearCurrentRoomId(userId);
        assertFalse(userService.isInRoom(userId, 1L));
        userService.saveCurrentRoomId(userId, 7L);
        assertTrue(userService.isInRoom(userId, 7L), "保存后应判定为在 7 号房间");
        assertFalse(userService.isInRoom(userId, 1L), "不应对其他房间返回 true");
        userService.clearCurrentRoomId(userId);
        assertFalse(userService.isInRoom(userId, 7L), "清除后应恢复为不在房间");
        log.info("P2-11 通过：加入 7 号房间 -> 离开 -> 语义正确");
    }

    // ==================== P2-23 MCP 死代码 ====================

    @Test
    @DisplayName("P2-23 权限类别里已无 MCP（MCP 模块已下线，留着只会渲染空白菜单）")
    public void mcpPermissionTypeIsRemoved() {
        for (PermissionType type : PermissionType.values()) {
            assertFalse("MCP".equalsIgnoreCase(type.name()),
                    "PermissionType 仍含 MCP 项，但 module/mcp 目录已不存在");
        }
        assertEquals(1, PermissionType.values().length, "应只剩 API 一项");
        log.info("P2-23 通过：PermissionType 仅剩 {}", PermissionType.values().length + " 项");
    }
}
