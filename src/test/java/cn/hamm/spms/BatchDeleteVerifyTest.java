package cn.hamm.spms;

import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.role.RoleMenuLinkService;
import cn.hamm.spms.module.personnel.role.RoleService;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.personnel.role.RoleMenuLinkEntity;
import cn.hamm.spms.module.personnel.role.RoleMenuLinkRepository;
import cn.hamm.spms.module.system.menu.MenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <h1>批量删除回归防护</h1>
 * <p>
 * 防的是「批量删除只有最后一条生效」这个曾经真实发生过的缺陷。
 * </p>
 * <p>
 * <b>成因</b>：{@code CurdService.delete(long id)} 第一步是 {@code get(id)}，
 * 而当时的 {@code CurdService.getById} 第一行是 {@code entityManager.clear()}。
 * 写 {@code forEach(this::delete)} 时，循环里每删一条都先 {@code get} 一次，
 * {@code clear()} 把上一条<b>已标记删除</b>的实体从持久化上下文丢弃，
 * 那条删除再也不会执行 —— 实测「删 2 条只删掉最后 1 条」，另一条永久残留。
 * </p>
 * <p>
 * <b>触发条件是「同一个事务里连续删多条」</b>。
 * 每条 {@code delete} 各自开一个事务时，删完即提交，
 * {@code clear()} 丢不掉任何东西，所以看不出问题。
 * 生产上删除都发生在钩子里（如 {@code ContractService.afterAppUpdate}），
 * 那时整个更新共用一个事务，多条删除挤在一起，缺陷才暴露。
 * <b>本类因此显式用 {@link TransactionTemplate} 把多次删除包进同一事务。</b>
 * </p>
 * <p>
 * <b>危害</b>：静默失败，无异常无日志。生产现场表现为
 * 「编辑工艺一次，工序多留一批」「授权反复点几次，菜单权限越攒越多」，
 * 数据只增不减、永远不会自己恢复。
 * </p>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class BatchDeleteVerifyTest {

    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private RoleMenuLinkService roleMenuLinkService;
    @Autowired
    private RoleMenuLinkRepository roleMenuLinkRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("同一事务内连续清空多条菜单授权，必须全部删除干净")
    public void clearingManyAuthorizationsInOneTransactionRemovesAll() {
        RoleEntity role = roleService.addAndGet(new RoleEntity().setName("批量删除-" + UUID.randomUUID()));
        List<MenuEntity> menus = menuRepository.findAll().stream().limit(4).toList();
        assertTrue(menus.size() >= 2, "前置：至少需要 2 个菜单才能验证批量删除");

        roleService.authorizeMenu(role, Set.copyOf(menus));
        assertEquals(menus.size(), roleService.get(role.getId()).getMenuList().size(),
                "前置：授权应全部落库");
        log.info("已授权 {} 个菜单", menus.size());

        // 关键：把多条删除放进同一个事务，且删除之间<b>不夹任何查询</b>。
        // 夹了查询也没关系 —— 查询会触发 auto-flush 把待执行的删除刷进库，
        // 反而掩盖掉缺陷。生产上删除是连续发生的，所以必须先取齐 ID 再连删。
        List<Long> linkIds = transactionTemplate.execute(status -> {
            Set<MenuEntity> authorized = roleService.get(role.getId()).getMenuList();
            log.info("同一事务内准备删除 {} 条授权关联", authorized.size());
            return authorized.stream()
                    .map(menu -> findLinkId(role.getId(), menu.getId()))
                    .toList();
        });
        log.info("待删除的关联 ID：{}", linkIds);
        transactionTemplate.executeWithoutResult(status ->
                linkIds.forEach(roleMenuLinkService::delete));

        assertEquals(0, roleService.get(role.getId()).getMenuList().size(),
                "同一事务内清空授权应删除全部 " + menus.size() + " 条关联"
                        + " —— 批量删除漏删，说明 getById 的 entityManager.clear() 被重新引入");
    }

    @Test
    @DisplayName("同一事务内连续解除 3 条以上授权，不能只删掉最后一条")
    public void revokingManyAuthorizationsInOneTransactionRemovesAll() {
        RoleEntity role = roleService.addAndGet(new RoleEntity().setName("逐条解除-" + UUID.randomUUID()));
        List<MenuEntity> menus = menuRepository.findAll().stream().limit(4).toList();
        assertTrue(menus.size() >= 3, "前置：至少需要 3 个菜单");
        roleService.authorizeMenu(role, Set.copyOf(menus));

        // 先取齐要删的 ID（保留第 1 个），再在同一事务里连删，删除之间不夹查询
        List<Long> staleIds = transactionTemplate.execute(status -> menus.stream()
                .filter(menu -> !menu.getId().equals(menus.get(0).getId()))
                .map(menu -> findLinkId(role.getId(), menu.getId()))
                .toList());
        log.info("待删除的关联 ID：{}", staleIds);
        transactionTemplate.executeWithoutResult(status ->
                staleIds.forEach(roleMenuLinkService::delete));

        Set<Long> left = roleService.get(role.getId()).getMenuList().stream()
                .map(MenuEntity::getId)
                .collect(java.util.stream.Collectors.toSet());
        log.info("解除 {} 条后剩余：{}（应只剩 1 个）", menus.size() - 1, left);

        assertEquals(Set.of(menus.get(0).getId()), left,
                "应只剩保留的那 1 个授权，旧的 " + (menus.size() - 1) + " 条必须全部删除");
    }

    @Test
    @DisplayName("同一事务内反复授权再清空 3 轮，中间表不得累积")
    public void repeatedAuthorizeAndClearDoesNotAccumulate() {
        RoleEntity role = roleService.addAndGet(new RoleEntity().setName("反复-" + UUID.randomUUID()));
        List<MenuEntity> menus = menuRepository.findAll().stream().limit(3).toList();

        for (int round = 1; round <= 3; round++) {
            roleService.authorizeMenu(new RoleEntity().setId(role.getId()), Set.copyOf(menus));
            assertEquals(menus.size(), roleService.get(role.getId()).getMenuList().size(),
                    "第 " + round + " 轮授权后数量不对");

            // 清空同样放在一个事务里
            transactionTemplate.executeWithoutResult(status -> roleService.get(role.getId())
                    .getMenuList()
                    .forEach(menu -> roleMenuLinkService
                            .delete(findLinkId(role.getId(), menu.getId()))));

            int left = roleService.get(role.getId()).getMenuList().size();
            log.info("第 {} 轮清空后剩余 {} 条", round, left);
            assertEquals(0, left,
                    "第 " + round + " 轮清空后残留 " + left + " 条 —— 反复操作会累积脏数据");
        }
    }

    /**
     * 查出某个角色与菜单之间那条关联记录的 ID
     *
     * @param roleId 角色 ID
     * @param menuId 菜单 ID
     * @return 关联记录 ID
     */
    private long findLinkId(long roleId, long menuId) {
        List<RoleMenuLinkEntity> links = roleMenuLinkRepository.findByRoleIds(List.of(roleId));
        return links.stream()
                .filter(l -> l.getMenu() != null && l.getMenu().getId() == menuId)
                .map(RoleMenuLinkEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "未找到角色 " + roleId + " 与菜单 " + menuId + " 的关联记录"));
    }
}
