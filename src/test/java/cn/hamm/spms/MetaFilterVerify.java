package cn.hamm.spms;

import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.RootModel;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;

import java.util.List;
import java.util.Set;

/**
 * <h1>专项实测 · @Meta 过滤引擎的三条路径</h1>
 * <p>
 * 直接运行：<br>
 * {@code java -cp <项目classpath> cn.hamm.spms.MetaFilterVerify}
 * </p>
 * <p>设计语义：whiteList 里的类 = 自身全字段返回（默认行为）；挂载对象 = 按 @Meta 过滤。</p>
 */
public class MetaFilterVerify {

    private static void line(String t) {
        System.out.println();
        System.out.println("======== " + t + " ========");
    }

    public static void main(String[] args) {
        traceJar();

        // ============ 路径 1：框架自动 CRUD（实体自身在白名单）============
        line("路径1：excludeNotMetaAndDesensitize([UserEntity], true) —— 框架自动接口");
        UserEntity a = buildUser();
        DepartmentEntity dept = buildDept();
        RoleEntity role = new RoleEntity();
        role.setId(20L).setName("财务管理员").setCode("FINANCE_ADMIN");
        a.setDepartmentList(Set.of(dept));
        a.setRoleList(Set.of(role));

        a.excludeNotMetaAndDesensitize(List.of(UserEntity.class), true);

        System.out.println("  [实体自身 UserEntity —— 设计如此，全部返回]");
        System.out.println("    nickname = " + a.getNickname());
        System.out.println("    email    = " + a.getEmail());
        System.out.println("  [挂载对象 DepartmentEntity —— 应按 @Meta 过滤]");
        DepartmentEntity od = a.getDepartmentList().iterator().next();
        System.out.println("    code(@Meta)      = " + od.getCode() + "   <- 应保留");
        System.out.println("    children(无注解) = " + od.getChildren() + "   <- 应过滤为 null");
        System.out.println("  [挂载对象 RoleEntity —— 应按 @Meta 过滤]");
        RoleEntity or = a.getRoleList().iterator().next();
        System.out.println("    name(@Meta)       = " + or.getName() + "   <- 应保留");
        System.out.println("    menuList(无注解)  = " + or.getMenuList() + "   <- 应过滤为 null");
        System.out.println("    permissionList    = " + or.getPermissionList() + "   <- 应过滤为 null");
        boolean nestedOk = (od.getChildren() == null) && (or.getMenuList() == null);
        System.out.println("  >> 挂载对象 @Meta 过滤 = " + (nestedOk ? "生效" : "失效"));

        // ============ 路径 2：无参 excludeNotMeta() —— SPMS 实际调用 ============
        line("路径2：无参 excludeNotMeta() —— SPMS 6 处实际调用");
        UserEntity b = buildUser();
        b.excludeNotMeta();
        System.out.println("  nickname(@Meta)   = " + b.getNickname() + "   <- 应保留");
        System.out.println("  email   (无注解)  = " + b.getEmail() + "   <- 应过滤");
        System.out.println("  realName(Desens.) = " + b.getRealName() + "   <- 应过滤");
        System.out.println("  idCard  (Desens.) = " + b.getIdCard() + "   <- 应过滤");
        boolean noArgOk = (b.getEmail() == null);
        System.out.println("  >> 无参 excludeNotMeta() = " + (noArgOk ? "生效" : "失效"));

        line("路径2 泄露示例：AppWebSocketHandler 推送给聊天室成员的内容");
        System.out.println("  " + Json.toString(b));

        line("路径2 泄露示例：DeviceController.getDeviceConfig（接口免登录）");
        DeviceEntity dev = new DeviceEntity();
        dev.setId(3L).setCode("DEV-003").setName("3号产线采集器").setUuid("uuid-abc-123");
        dev.excludeNotMeta();
        System.out.println("  uuid = " + dev.getUuid() + "   <- 设备身份凭据，应被过滤");
        System.out.println("  " + Json.toString(dev));

        // ============ 汇总 ============
        line("汇总");
        System.out.println("挂载对象 @Meta 过滤（路径1） = " + (nestedOk ? "生效" : "失效"));
        System.out.println("无参 excludeNotMeta()（路径2） = " + (noArgOk ? "生效" : "失效"));
        System.out.println();
        System.out.println(">>> 根因：运行时加载的是 " + jarName()
                + "，其 excludeNotMeta() 无参版把 List.of(自身类) 传入白名单，");
        System.out.println(">>>       导致 !isEmpty() && !contains(自身) == false，一个字段都不排除。");
    }

    private static void traceJar() {
        line("运行时 jar 溯源");
        System.out.println("  RootModel -> " + RootModel.class.getProtectionDomain()
                .getCodeSource().getLocation());
        boolean hasVisited = false;
        for (var m : RootModel.class.getDeclaredMethods()) {
            if (m.getName().equals("excludeNotMetaAll")) { hasVisited = true; }
        }
        System.out.println("  含 excludeNotMetaAll(带 visited 防环)？ " + hasVisited
                + (hasVisited ? "" : "   <== 旧版 jar，修复未发布"));
    }

    private static String jarName() {
        String loc = RootModel.class.getProtectionDomain().getCodeSource().getLocation().toString();
        return loc.substring(loc.lastIndexOf('/') + 1);
    }

    private static UserEntity buildUser() {
        return (UserEntity) new UserEntity()
                .setId(7L).setNickname("李四").setAvatar("a.png")
                .setRealName("李四").setIdCard("310101199202022345")
                .setEmail("lisi@company.com")
                .setGender(1).setPassword("HASH").setSalt("SALT");
    }

    private static DepartmentEntity buildDept() {
        DepartmentEntity d = new DepartmentEntity();
        d.setId(10L).setCode("D001").setName("财务部").setParentId(1L).setOrderNo(5);
        DepartmentEntity c = new DepartmentEntity();
        c.setId(11L).setCode("D001-1").setName("财务一组");
        d.setChildren(List.of(c));
        return d;
    }
}
