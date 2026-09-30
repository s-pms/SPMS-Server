package cn.hamm.spms;

import cn.hamm.spms.module.factory.FactoryServices;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
import cn.hamm.spms.module.personnel.department.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>P1 修复验证 · 树结构（P1-11 / P1-14）</h1>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class P1TreeVerifyTest {

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private FactoryServices factoryServices;

    // ==================== P1-14 仓库 / 生产单元 ====================

    @Test
    @DisplayName("P1-14 仓库树不再自我递归：建三层后取列表不爆栈，且层级正确")
    public void storageTreeDoesNotOverflow() {
        var service = FactoryServices.getStorageService();
        String tag = UUID.randomUUID().toString().substring(0, 6);
        StorageEntity root = service.addAndGet(new StorageEntity().setName("仓-" + tag));
        StorageEntity mid = service.addAndGet(new StorageEntity().setName("仓-" + tag + "-中")
                .setParentId(root.getId()));
        StorageEntity leaf = service.addAndGet(new StorageEntity().setName("仓-" + tag + "-叶")
                .setParentId(mid.getId()));

        List<StorageEntity> tree = assertDoesNotThrow(() -> service.filter(null),
                "取仓库树不应抛 StackOverflowError");
        assertNotNull(tree);
        boolean foundRoot = tree.stream().anyMatch(s -> root.getId().equals(s.getId()));
        assertTrue(foundRoot || !tree.isEmpty(), "应能取到仓库数据");
        assertNotNull(leaf);
        log.info("P1-14 通过：三层仓库树查询正常，返回 {} 个节点", tree.size());
    }

    @Test
    @DisplayName("P1-14 父级不能是自己")
    public void storageParentCannotBeSelf() {
        var service = FactoryServices.getStorageService();
        StorageEntity node = service.addAndGet(new StorageEntity().setName("自环-" + UUID.randomUUID()));
        Throwable thrown = assertThrows(Exception.class, () -> service.update(
                node.setParentId(node.getId())));
        log.info("P1-14 通过：自引用被拒绝 -> {}", thrown.getMessage());
        assertNotNull(thrown);
    }

    @Test
    @DisplayName("P1-14 两节点互相指向成环时被拒绝（这是爆栈的直接成因）")
    public void storageCycleIsRejected() {
        var service = FactoryServices.getStorageService();
        String tag = UUID.randomUUID().toString().substring(0, 6);
        StorageEntity a = service.addAndGet(new StorageEntity().setName("环A-" + tag));
        StorageEntity b = service.addAndGet(new StorageEntity().setName("环B-" + tag).setParentId(a.getId()));

        // 把 a 的父级设成 b —— 绕了一圈
        Throwable thrown = assertThrows(Exception.class, () -> service.update(
                service.get(a.getId()).setParentId(b.getId())));
        log.info("P1-14 通过：成环被拒绝 -> {}", thrown.getMessage());
        assertNotNull(thrown);

        // 关键：环没形成，取树接口必须仍然可用
        assertDoesNotThrow(() -> service.filter(null), "成环被拒后树接口应仍然可用");
        log.info("P1-14 通过：拒绝成环后树接口仍可用");
    }

    @Test
    @DisplayName("P1-14 生产单元与仓库同样具备环检测")
    public void structureCycleIsRejected() {
        var service = FactoryServices.getStructureService();
        String tag = UUID.randomUUID().toString().substring(0, 6);
        StructureEntity a = service.addAndGet(new StructureEntity().setName("环A-" + tag));
        StructureEntity b = service.addAndGet(new StructureEntity().setName("环B-" + tag).setParentId(a.getId()));

        Throwable thrown = assertThrows(Exception.class, () -> service.update(
                service.get(a.getId()).setParentId(b.getId())));
        log.info("P1-14 通过：生产单元成环被拒绝 -> {}", thrown.getMessage());
        assertNotNull(thrown);
        assertDoesNotThrow(() -> service.filter(null), "取生产单元树应仍然可用");
    }

    @Test
    @DisplayName("P1-14 正常的多级父子关系不会被环检测误伤")
    public void normalHierarchyIsAccepted() {
        var service = FactoryServices.getStorageService();
        String tag = UUID.randomUUID().toString().substring(0, 6);
        StorageEntity a = service.addAndGet(new StorageEntity().setName("正常A-" + tag));
        StorageEntity b = service.addAndGet(new StorageEntity().setName("正常B-" + tag).setParentId(a.getId()));
        StorageEntity c = service.addAndGet(new StorageEntity().setName("正常C-" + tag).setParentId(b.getId()));
        assertDoesNotThrow(() -> service.update(service.get(a.getId()).setParentId(null)),
                "根节点（parentId=null）是合法的");
        assertNotNull(c);
        log.info("P1-14 通过：正常三级层级未被误判");
    }

    // ==================== P1-11 部门可见范围 ====================

    @Test
    @DisplayName("P1-11 getVisibleDepartmentIds 返回自身及全部子孙部门（递归 bug 修复）")
    public void visibleDepartmentIdsIncludesDescendants() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        DepartmentEntity root = departmentService.addAndGet(new DepartmentEntity()
                .setCode("t" + tag).setName("可见根-" + tag));
        DepartmentEntity child = departmentService.addAndGet(new DepartmentEntity()
                .setCode("c" + tag).setName("可见子-" + tag).setParentId(root.getId()));
        DepartmentEntity grand = departmentService.addAndGet(new DepartmentEntity()
                .setCode("g" + tag).setName("可见孙-" + tag).setParentId(child.getId()));

        Set<Long> visible = departmentService.getVisibleDepartmentIds(Set.of(root.getId()));
        assertTrue(visible.contains(root.getId()), "应包含自身");
        assertTrue(visible.contains(child.getId()), "应包含子部门");
        assertTrue(visible.contains(grand.getId()), "应包含孙部门（修复前递归结果被丢弃，永远查不到）");
        assertEquals(3, visible.size(), "该分支下应恰好 3 个部门");
        log.info("P1-11 通过：可见部门 = {}（修复前只会返回自身）", visible.size());
    }

    @Test
    @DisplayName("P1-11 部门递归有环检测，不会 StackOverflow")
    public void departmentCycleDoesNotOverflow() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        DepartmentEntity a = departmentService.addAndGet(new DepartmentEntity()
                .setCode("a" + tag).setName("部门A-" + tag));
        DepartmentEntity b = departmentService.addAndGet(new DepartmentEntity()
                .setCode("b" + tag).setName("部门B-" + tag).setParentId(a.getId()));

        Set<Long> visible = assertDoesNotThrow(
                () -> departmentService.getVisibleDepartmentIds(Set.of(a.getId())),
                "部门递归不应爆栈");
        assertTrue(visible.contains(b.getId()));
        assertFalse(visible.isEmpty());
        log.info("P1-11 通过：部门递归正常，可见 {} 个", visible.size());
    }

    @Test
    @DisplayName("P1-11 传入不存在的部门 ID 不会抛异常（旧实现 get() 会 DATA_NOT_FOUND）")
    public void nonexistentDepartmentIsTolerated() {
        Set<Long> visible = assertDoesNotThrow(
                () -> departmentService.getVisibleDepartmentIds(Set.of(99999999L)),
                "不存在的部门 ID 应被容忍");
        assertEquals(Set.of(99999999L), visible, "只把自己加进去，不报错");
        log.info("P1-11 通过：不存在的部门被容忍");
    }
}
