package cn.hamm.spms;

import cn.hamm.spms.module.asset.contract.ContractEntity;
import cn.hamm.spms.module.asset.contract.ContractService;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.factory.FactoryServices;
import cn.hamm.spms.module.mes.bom.BomEntity;
import cn.hamm.spms.module.mes.bom.BomService;
import cn.hamm.spms.module.mes.operation.OperationService;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.role.RoleService;
import cn.hamm.spms.module.personnel.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * <h1>专项验证 · open-in-view 关闭后的真实影响</h1>
 * <p>
 * 单元测试直接调 Service，不经过 Jackson 序列化，因此<b>测不出</b> LAZY 关联的加载问题。
 * 本类打真实的 HTTP 接口，让框架走完整的
 * {@code 拦截器 -> Controller -> Json.data(entity) -> Jackson 序列化} 链路，
 * 这才是 {@code open-in-view: false} 真正的风险点。
 * </p>
 * <p>
 * ⚠️ <b>关键</b>：必须先造出带关联的数据。空表不会触发懒加载，
 * 探测结果会假阴性（2026-09-30 首次探测时合同表是空的，误判为「不受影响」）。
 * </p>
 */
@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("local-hamm")
public class OpenInViewVerifyTest {

    private static final long ROOT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private BomService bomService;
    @Autowired
    private OperationService operationService;

    @BeforeEach
    void seedDataWithAssociations() {
        // 用户 -> 角色（UserEntity.roleList 是 LAZY 多对多）
        if (roleService.filter(null).isEmpty()) {
            roleService.addAndGet(new RoleEntity().setName("探测角色-" + System.nanoTime()));
        }
        // 合同 -> 参与方/附件（ContractEntity.participantList / documentList 是 LAZY）
        if (contractService.filter(null).isEmpty()) {
            contractService.addAndGet(new ContractEntity()
                    .setName("探测合同-" + System.nanoTime()));
        }
        // 设备 -> 参数（DeviceEntity.parameters 是 LAZY）
        if (deviceService.filter(null).isEmpty()) {
            deviceService.addAndGet(new DeviceEntity()
                    .setName("探测设备-" + System.nanoTime()));
        }
        // BOM -> 明细（BomEntity.details 是 LAZY）
        if (bomService.filter(null).isEmpty()) {
            bomService.addAndGet(new BomEntity().setName("探测BOM-" + System.nanoTime()));
        }
        // 角色 -> 菜单/权限（RoleEntity.menuList / permissionList 是 LAZY）
        log.info("探测数据已准备: 角色 {} 合同 {} 设备 {} BOM {}",
                roleService.filter(null).size(), contractService.filter(null).size(),
                deviceService.filter(null).size(), bomService.filter(null).size());
    }

    /**
     * 打一次列表或详情查询并记录结果
     *
     * @param api     接口路径
     * @param payload 请求体
     * @param label   日志标签
     * @return 响应体
     * @throws Exception 请求异常
     */
    private String probe(String api, String payload, String label) throws Exception {
        String token = userService.createAccessToken(ROOT_USER_ID);
        MvcResult result = mockMvc.perform(post("/" + api)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        boolean lazyError = body.contains("LazyInitialization") || body.contains("could not initialize proxy");
        log.info("[{}] HTTP {} | 长度 {} | 懒加载异常 {}", label,
                result.getResponse().getStatus(), body.length(), lazyError);
        return body;
    }

    @Test
    @DisplayName("open-in-view 关闭后，带关联数据的真实 HTTP 接口仍可正常序列化")
    public void httpSerializationStillWorksWithoutOpenInView() throws Exception {
        int failures = 0;

        // ① 用户：roleList / departmentList 是 LAZY 多对多（登录后前端必拉）
        failures += check(probe("user/getList", "{\"page\":{\"current\":1,\"size\":5}}", "用户列表-角色部门"), "用户列表");
        failures += check(probe("user/getDetail", "{\"id\":" + ROOT_USER_ID + "}", "用户详情-角色部门"), "用户详情");

        // ② 角色：menuList / permissionList 是 LAZY
        failures += check(probe("role/getList", "{\"page\":{\"current\":1,\"size\":5}}", "角色列表-菜单权限"), "角色列表");

        // ③ 合同：participantList / documentList 是 LAZY
        failures += check(probe("contract/getList", "{\"page\":{\"current\":1,\"size\":5}}", "合同列表-参与方"), "合同列表");
        failures += check(probe("contract/getDetail", "{\"id\":1}", "合同详情-ExposeAll"), "合同详情");

        // ④ 设备：parameters 是 LAZY
        failures += check(probe("device/getList", "{\"page\":{\"current\":1,\"size\":5}}", "设备列表-参数"), "设备列表");

        // ⑤ BOM：details 是 LAZY
        failures += check(probe("bom/getList", "{\"page\":{\"current\":1,\"size\":5}}", "BOM列表-明细"), "BOM列表");

        // ⑥ 生产单元：operationList 是 LAZY
        failures += check(probe("structure/getList", "{}", "生产单元-工序"), "生产单元");

        // ⑦ 对照组：单表实体 + 瞬态明细
        failures += check(probe("material/getList", "{\"page\":{\"current\":1,\"size\":5}}", "物料-单表对照"), "物料");
        failures += check(probe("routing/getList", "{\"page\":{\"current\":1,\"size\":5}}", "工艺-瞬态明细"), "工艺");
        failures += check(probe("department/getList", "{}", "部门-可见范围"), "部门");
        failures += check(probe("input/getList", "{\"page\":{\"current\":1,\"size\":5}}", "入库单-明细"), "入库单");

        log.info("==== open-in-view 探测结果：{} 个接口出现异常 ====", failures);
        // 本测试的意义在于「守住可用性」。
        // 一旦有人动 open-in-view 或 LAZY 关联配置，本用例会立刻失败并指出受影响的具体接口。
        assertEquals(0, failures,
                "有接口出现异常，说明 open-in-view 或 LAZY 关联配置发生了变化，需要重新评估");
    }

    /**
     * 判断响应是否异常
     *
     * @param body  响应体
     * @param label 标签
     * @return 异常时返回 1
     */
    private int check(String body, String label) {
        if (body.contains("LazyInitialization") || body.contains("could not initialize proxy")) {
            log.warn("[{}] ⚠ 懒加载异常: {}", label, body.substring(0, Math.min(300, body.length())));
            return 1;
        }
        if (body.contains("\"code\":500")) {
            log.warn("[{}] ⚠ 500: {}", label, body.substring(0, Math.min(300, body.length())));
            return 1;
        }
        return 0;
    }
}
