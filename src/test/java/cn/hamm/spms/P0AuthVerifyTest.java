package cn.hamm.spms;

import cn.hamm.airpower.core.AccessTokenUtil;
import cn.hamm.airpower.core.constant.Constant;
import cn.hamm.airpower.curd.permission.PermissionUtil;
import cn.hamm.spms.SpmsApplication;
import cn.hamm.spms.common.interceptor.RequestInterceptor;
import cn.hamm.spms.module.personnel.role.RoleController;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.role.RoleService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.system.file.FileController;
import cn.hamm.spms.module.system.file.FileEntity;
import cn.hamm.spms.module.system.file.enums.FileCategory;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import cn.hamm.spms.module.system.permission.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P0 修复验证 · 鉴权与账号类（P0-1 / P0-2 / P0-3 / P0-8）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P0AuthVerifyTest {

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;
    @Autowired
    private RequestInterceptor requestInterceptor;

    private static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@spms.local";
    }

    @Test
    @DisplayName("P0-1 代码扫描出的权限标识必须与权限表逐条对得上（修复前格式漂移导致全部查不到 → NPE 500）")
    public void permissionIdentityMatchesDatabase() {
        List<PermissionEntity> scanned =
                PermissionUtil.scanPermission(SpmsApplication.class.getPackageName(), PermissionEntity.class);
        log.info("代码扫描到权限（含子权限）数量: {}", countAll(scanned));
        assertFalse(scanned.isEmpty(), "代码扫描不应得到空权限列表");

        int missing = 0;
        for (PermissionEntity p : scanned) {
            if (Objects.isNull(permissionService.getPermissionByIdentity(p.getIdentity()))) {
                missing++;
                log.error("权限标识在表中不存在: {}", p.getIdentity());
            }
        }
        assertEquals(0, missing, "存在 " + missing + " 条权限标识未落库，非超管用户会全部 500");
    }

    @Test
    @DisplayName("P0-1 permission-with-package=true 已生效：标识含包名，跨模块唯一且可在权限表查到")
    public void permissionIdentityCarriesPackageName() throws NoSuchMethodException {
        var method = RoleController.class.getMethod("authorizeMenu", RoleEntity.class);
        String identity = PermissionUtil.getPermissionIdentity(RoleController.class, method);
        log.info("role/authorizeMenu 的权限标识 = {}", identity);
        assertTrue(identity.startsWith("module.personnel.role.Role:"),
                "标识应形如 module.personnel.role.Role:authorizeMenu，实际为 " + identity);
        assertNotNull(permissionService.getPermissionByIdentity(identity), "该标识应能在权限表中查到");
    }

    @Test
    @DisplayName("P0-1 标识查不到时抛业务异常而非 NullPointerException（这正是修复前全线 500 的根因）")
    public void checkUserPermissionDoesNotThrowNpe() {
        // id=2 是 dev 初始化的非超管用户「张三」
        AccessTokenUtil.VerifiedToken token = new AccessTokenUtil.VerifiedToken()
                .setPayloads(Map.of(Constant.ID, 2L));
        Throwable thrown = assertDoesNotThrow(() -> {
            try {
                requestInterceptor.checkUserPermission(token, "definitely.not.exists:xxx", null);
            } catch (NullPointerException e) {
                throw new AssertionError("仍抛出 NullPointerException，修复无效", e);
            } catch (Exception e) {
                log.info("如期抛出业务异常: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            }
            return null;
        });
        assertTrue(Objects.isNull(thrown));
    }

    @Test
    @Transactional
    @DisplayName("P0-2 角色授权真的落库（修复前 beforeAppUpdate 把关联置空，接口仍返回成功）")
    public void roleAuthorizeMenuReallyPersists() {
        String roleName = "P0角色-" + UUID.randomUUID();
        RoleEntity role = roleService.addAndGet(new RoleEntity().setName(roleName));
        List<MenuEntity> menus = menuService.filter(null);
        assertFalse(menus.isEmpty(), "应至少存在一个菜单");
        MenuEntity target = menus.get(0);

        roleService.authorizeMenu(role, Set.of(target));
        RoleEntity reloaded = roleService.get(role.getId());
        assertNotNull(reloaded.getMenuList(), "授权后 menuList 不应为 null");
        assertTrue(reloaded.getMenuList().stream().anyMatch(m -> target.getId().equals(m.getId())),
                "授权的菜单未落库");
        log.info("角色 {} 授权菜单数 = {}", reloaded.getId(), reloaded.getMenuList().size());
    }

    @Test
    @Transactional
    @DisplayName("P0-2 角色授权权限真的落库")
    public void roleAuthorizePermissionReallyPersists() {
        String roleName = "P0权限角色-" + UUID.randomUUID();
        RoleEntity role = roleService.addAndGet(new RoleEntity().setName(roleName));
        PermissionEntity target = permissionService.filter(null).get(0);

        roleService.authorizePermission(role, Set.of(target));
        RoleEntity reloaded = roleService.get(role.getId());
        assertNotNull(reloaded.getPermissionList(), "授权后 permissionList 不应为 null");
        assertTrue(reloaded.getPermissionList().stream().anyMatch(p -> target.getId().equals(p.getId())),
                "授权的权限未落库");
        log.info("角色 {} 授权权限数 = {}", reloaded.getId(), reloaded.getPermissionList().size());
    }

    @Test
    @DisplayName("P0-2 通用 update 不允许改关联，避免绕过专用授权接口")
    public void genericUpdateRejectsAssociationChange() {
        String roleName = "P0通用更新-" + UUID.randomUUID();
        RoleEntity role = roleService.addAndGet(new RoleEntity().setName(roleName));
        RoleEntity patch = new RoleEntity().setId(role.getId()).setName(roleName).setMenuList(Set.of());
        Exception thrown = null;
        try {
            roleService.update(patch);
        } catch (Exception e) {
            thrown = e;
        }
        assertNotNull(thrown, "通用 update 携带 menuList 时应被拒绝，而不是静默忽略");
        log.info("通用 update 被正确拒绝: {}", thrown.getMessage());
    }

    @Test
    @DisplayName("P0-3 邮箱注册写入 email 字段（修复前 email 落空，导致登录覆盖已有用户）")
    public void registerViaEmailPersistsEmail() {
        String email = uniqueEmail("p0verify");
        UserEntity created = userService.registerUserViaEmail(email);
        assertEquals(email, created.getEmail(), "注册后 email 必须落库");
        log.info("邮箱注册验证通过，userId={}, email={}", created.getId(), created.getEmail());
    }

    @Test
    @DisplayName("P0-3 重复注册同一邮箱被唯一索引拦下（修复前 email 恒为空会静默产生多个空邮箱账号）")
    public void duplicateEmailIsRejectedByUniqueIndex() {
        String email = uniqueEmail("p0dup");
        userService.registerUserViaEmail(email);
        Exception thrown = null;
        try {
            userService.registerUserViaEmail(email);
        } catch (Exception e) {
            thrown = e;
        }
        assertNotNull(thrown, "重复邮箱应被拒绝");
        log.info("重复邮箱被拒绝: {}", thrown.getClass().getSimpleName());
    }

    @Test
    @DisplayName("P0-8 文件下载接口不再免登录")
    public void fileDownloadRequiresLogin() throws NoSuchMethodException {
        var method = FileController.class.getMethod("getFileUrl", String.class,
                jakarta.servlet.http.HttpServletResponse.class);
        cn.hamm.airpower.curd.permission.Permission permission =
                method.getAnnotation(cn.hamm.airpower.curd.permission.Permission.class);
        assertNotNull(permission, "应标注 @Permission");
        assertTrue(permission.login(), "login 必须为 true，修复前为 false（匿名可遍历下载全站附件）");
        log.info("getFileUrl 的 @Permission.login = {}", permission.login());
    }

    @Test
    @DisplayName("P0-8 受保护文件类别具备可用的归属校验字段")
    public void protectedFileHasUploader() {
        assertTrue(Boolean.TRUE.equals(FileCategory.CONTRACT_ATTACHMENT.getIsProtected()),
                "合同附件应为受保护类别");
        FileEntity file = new FileEntity().setUploader(new UserEntity().setId(7L));
        assertNotNull(file.getUploader(), "文件实体应有上传人字段");
        assertEquals(7L, file.getUploader().getId());
        log.info("FileEntity.uploader 就位，可用于受保护文件的归属校验");
    }

    private long countAll(List<PermissionEntity> list) {
        if (Objects.isNull(list)) {
            return 0L;
        }
        return list.stream().mapToLong(p -> 1L + countAll(p.getChildren())).sum();
    }
}
