# SPMS Server · AI Agent 协作规范

> 本文档是给 **AI Agent（Copilot / Cursor / Claude / Qwen Code 等）** 阅读并严格遵守的协作约定。
> 如果你是人类开发者，请阅读 [docs/DEV.md](./docs/DEV.md) 获取完整的开发指南。

---

## 0. 必读铁律（违反任意一条都视为不合格的产出）

| #   | 规则                                            | 原因                                                |
|-----|-----------------------------------------------|---------------------------------------------------|
| 0.1 | **不要修改 `base/` 包下的任何基类**                      | 基类是整个系统的骨架，扩展请通过继承                                |
| 0.2 | **不要绕过 `BaseService / BaseController` 自己写接口** | 所有权限、发布锁定、代码生成等能力都依赖基类                            |
| 0.3 | **不要在 Controller 里 `try/catch` 业务异常**         | 统一由 `RequestInterceptor` 处理                       |
| 0.4 | **不要新增硬编码异常码**                                | 一律添加到 `cn.hamm.spms.common.exception.CustomError` |
| 0.5 | **不要写新的 `@SpringBootApplication` 入口**         | 入口只有一个：`cn.hamm.spms.SpmsApplication`             |
| 0.6 | **不要在 Service 中持有 Bean 引用之外的实例变量**            | 否则 `@Scheduled` 任务会出现线程安全与状态污染                    |
| 0.7 | **不要空注释**，除非用户明确要求                            | 用户偏好（默认为方法添加标准的 JavaDoc）                          |
| 0.8 | **不要提交敏感信息**（密码、token、连接串等）                   | 默认配置模板中所有值都应被替换                                   |

---

## 1. 项目快速画像

| 维度   | 值                                                                             |
|------|-------------------------------------------------------------------------------|
| 项目名  | `cn.hamm.spms` / artifactId `server` / version `4.0.0`                        |
| 父框架  | `cn.hamm:airpower:8.0.0`（**不要绕过 AirPower 自己造轮子**）                             |
| 技术栈  | Java 17 + Spring Boot 3 + JPA + Hibernate + MySQL 8 + Redis + InfluxDB + MQTT |
| 构建工具 | Maven（项目自带 `mvnw`）                                                            |
| 容器   | Docker（`amazoncorretto:17-alpine`）                                            |
| 包前缀  | `cn.hamm.spms`                                                                |
| 启动类  | `cn.hamm.spms.SpmsApplication`                                                |
| 默认端口 | `8080`                                                                        |

启动成功标志：控制台输出 `Hi Guy, SPMS is running at [8080] !`（`SpmsApplication.java:34`）

---

## 2. 目录地图（必背）

```
src/main/java/cn/hamm/spms/
├── SpmsApplication.java          # 入口（@EnableWebSocket + @EnableScheduling）
├── SpmsWebConfig.java            # Web & WebSocket 配置
├── SpmsDevData.java              # 开发者模式数据初始化（CommandLineRunner）
├── base/                         # ⛔️ 不要修改
│   ├── BaseEntity.java           # 含 isPublished 字段
│   ├── BaseRepository.java
│   ├── BaseService.java          # 含 publish()、beforeSaveToDatabase 钩子
│   ├── BaseController.java       # 含 publish 接口 + 发布后拦截
│   └── bill/                     # 单据基类（AbstractBaseBill* + BaseBillController）
│       └── detail/               # 单据明细基类（BaseBillDetail*）
├── common/                       # 应用层通用
│   ├── AppConfig.java            # app.* 配置
│   ├── AppConstant.java          # 常量（含 BASE_CUSTOM_ERROR = 200000）
│   ├── Configs.java              # 静态持有 AirPower Config + AppConfig
│   ├── AppWebSocketHandler.java  # 房间聊天业务逻辑
│   ├── annotation/AutoGenerateCode.java
│   ├── cron/                     # @Scheduled 任务
│   ├── exception/CustomError.java
│   ├── influx/                   # InfluxDB 配置 + 助手
│   └── interceptor/RequestInterceptor.java
└── module/                       # 业务模块（每个模块一个 XxxServices.java）
    ├── system/                   # 菜单 / 权限 / 编码规则 / 配置 / 单位 / 文件
    ├── personnel/                # 用户 / 角色 / 部门
    ├── asset/                    # 物料 / 设备 / 合同
    ├── channel/                  # 客户 / 供应商 / 采购 / 销售 / 价格
    ├── factory/                  # 仓库 / 生产单元
    ├── mes/                      # 生产计划 / 订单 / 领料 / BOM / 工序 / 工艺
    ├── wms/                      # 入库 / 出库 / 移库 / 库存
    ├── iot/                      # 参数管理 / 设备数据上报
    ├── chat/                     # 房间 / 成员 / WebSocket
    ├── open/                     # OAuth2 / 第三方登录 / 通知
    ├── mcp/                      # MCP 工具注册中心
    └── wechat/                   # 微信扩展
```

---

## 3. 行为准则（DO / DON'T）

### 3.1 新增业务模块

**DO**：

- ✅ 路径：`module/<module>/<business>/`，包含五件套 `Entity / Service / Repository / Controller / enums/*`
- ✅ Entity 继承 `BaseEntity<E>`（普通）或 `AbstractBaseBillEntity<E, D>`（单据）
- ✅ Controller 继承 `BaseController` 或 `BaseBillController`
- ✅ Service 继承 `BaseService` 或 `AbstractBaseBillService`
- ✅ Repository 继承 `BaseRepository` 或 `BaseBillRepository`
- ✅ 在模块的 `XxxServices.java` 中追加 `@Getter private static XxxService xxxService;` 静态字段

**DON'T**：

- ❌ 不要创建独立的、不继承基类的 Controller
- ❌ 不要把 Service 放在 `common/` 包
- ❌ 不要在 `module/<module>/<business>/` 外创建业务类

### 3.2 Entity 编写规范

**DO**：

- ✅ 注解顺序：
  `@Entity @Table @Description @Data @EqualsAndHashCode(callSuper = true) @Accessors(chain = true) @DynamicInsert @DynamicUpdate`
- ✅ 字段加 `@Description("...")`
- ✅ 需要搜索的字段加 `@Search`
- ✅ 需要返回给前端的字段加 `@Meta`
- ✅ 字典字段加 `@Dictionary(EnumClass.class, groups = {WhenAdd.class, WhenUpdate.class})`
- ✅ 自动编号字段加 `@AutoGenerateCode(CodeRuleField.XXX)`
- ✅ 表名 = 实体名小写（关键字避开：`order` → `orders`）
- ✅ 字段注释：`columnDefinition = "varchar(255) default '' comment '字段注释'"`

**DON'T**：

- ❌ 不要省略 `@EqualsAndHashCode(callSuper = true)`
- ❌ 不要省略 `@DynamicInsert / @DynamicUpdate`
- ❌ 不要使用中文表名 / 字段名
- ❌ 不要手动 `import javax.persistence.*`（ **用 `jakarta.persistence.*`**）

参考：`module/asset/material/MaterialEntity.java`

### 3.3 Controller 编写规范

**DO**：

- ✅ 标注 `@Api("xxx")` 和 `@Description("xxx")`
- ✅ 需要扩展默认接口时使用 `@Extends({Curd.Xxx, ...})`
- ✅ 单据 Controller 继承 `BaseBillController`
- ✅ 公开接口加 `@Permission(login = false)` 或 `@Permission(authorize = false)`

**DON'T**：

- ❌ 不要在 Controller 里写业务逻辑（放进 Service）
- ❌ 不要 catch 业务异常（让它向上抛）
- ❌ 不要重复实现基类已提供的方法（`add / update / delete / get`）

### 3.4 Service 编写规范

**DO**：

- ✅ 用 `protected` 钩子重写业务逻辑：
  `beforeAppSaveToDatabase / beforeAppUpdate / beforeAppDelete / afterAppGet / afterAppAdd / afterAppUpdate / beforePublish`
- ✅ 单据 Service 重写：
  `afterBillAudited / afterBillFinished / afterAllBillDetailFinished / afterDetailFinishAdded / afterBillAdd / afterBillUpdate / beforeBillFinish`
- ✅ 单据必填的抽象方法：`getAuditingStatus() / getAuditedStatus() / getRejectedStatus() / getBillDetailsFinishStatus()`
- ✅ 跨模块调用：`SystemServices.getXxxService()` / `WmsServices.getInputService()` 等

**DON'T**：

- ❌ 不要重写 `final` 方法（`beforeSaveToDatabase / publish / beforeUpdate / beforeDelete`）
- ❌ 不要在重写钩子里手动调用 `super.xxx()`（基类已自动串联）
- ❌ 不要在单据 Service 中手动持久化明细（`AbstractBaseBillService.saveDetails` 已包办）

### 3.5 异常处理

**DO**：

- ✅ 使用 `cn.hamm.airpower.exception.Errors.*` 的静态工具：
  ```java
  import static cn.hamm.airpower.exception.Errors.*;
  // FORBIDDEN.when(obj.isPublished(), "已发布数据不允许编辑");
  // PARAM_INVALID.whenNull(obj, "对象不能为空");
  ```
- ✅ 新增业务异常时追加到 `cn.hamm.spms.common.exception.CustomError`
- ✅ 自定义异常码会自动加 `AppConstant.BASE_CUSTOM_ERROR (200000)` 基址

**DON'T**：

- ❌ 不要直接 `throw new RuntimeException(...)`
- ❌ 不要硬编码异常码数字（如 `throw new ServiceException(200101, "...")`）

### 3.6 鉴权与权限

- 默认所有 Controller 都走 `BaseController` 上的 `@Permission`，自动启用 RBAC
- 公开接口：`@Permission(login = false)`（无需登录）或 `@Permission(authorize = false)`（跳过权限校验）
- 超管判定：`UserEntity.isRootUser()` 当 `id == 1L` 放行所有权限
- 个人令牌：`@Permission(login = false)` + `RequestInterceptor.getVerifiedToken` 已处理

### 3.7 枚举

**DO**：

- ✅ 实现 `cn.hamm.airpower.core.interfaces.IDictionary`
- ✅ 提供 `getKey()`（int）和 `getLabel()`（String）
- ✅ 统一放在 `<business>/enums/` 包

---

## 4. 单据（Bill）开发快捷参考

> 涉及明细、有状态流转、数量进度的业务，必须使用单据抽象。

| 角色         | 基类                                                                                          |
|------------|---------------------------------------------------------------------------------------------|
| Entity     | `AbstractBaseBillEntity<E, D>`                                                              |
| Detail     | `BaseBillDetailEntity<D>`（实现 `getQuantity/setQuantity/getFinishQuantity/setFinishQuantity`） |
| Service    | `AbstractBaseBillService<E, R, D, DS, DR>`                                                  |
| Repository | `BaseBillRepository<E, D>`                                                                  |
| Controller | `BaseBillController<E, S, R, D, DS, DR>`                                                    |

**状态机**：

```
AUDITING ──audit──▶ AUDITED ──setBillDetailsAllFinished──▶ DETAILS_FINISHED ──setBillFinished──▶ DONE
   │                  ▲
   └──reject─► REJECTED ◀──canEdit── (回到驳回可再次编辑)
```

**关键规则**：

- 单据号字段加 `@AutoGenerateCode(CodeRuleField.OrderBillCode)`，由 `BaseService.beforeSaveToDatabase` 自动填充
- 需要自动审核：重写 `getAutoAuditConfigFlag()` 返回 `ConfigFlag.XXX`，dev 模式下后台启用即可
- 已驳回单据可编辑；其他状态禁止修改（`BaseBillController.beforeAppUpdate` 拦截）

---

## 5. 常用扩展任务清单

### 5.1 新增一种单据号编码类型

1. 在 `module/system/coderule/enums/CodeRuleField.java` 追加枚举项（含 `key / label / defaultPrefix / defaultSnType`）
2. dev 模式重启 → 自动初始化默认前缀模板

### 5.2 新增一种系统配置开关

1. 在 `module/system/config/enums/ConfigFlag.java` 追加枚举项（含 `key / label / type / defaultValue / description`）
2. dev 模式重启 → 自动初始化默认配置
3. 在需要使用的地方注入 `ConfigService`，调用 `get(ConfigFlag.XXX).booleanConfig()`

### 5.3 新增 MCP 工具

```java

@McpMethod(name = "查询物料", description = "按编码查询物料信息")
public MaterialEntity getMaterialByCode(@McpParam("编码") String code) {
    return repository.getByCode(code);
}
```

- 路径：任意 Service 方法
- 自动扫描包：`cn.hamm.spms` + `cn.hamm.airpower`（在 `SpmsDevData.java:93`）
- 权限自动注册到「MCP工具」菜单
- 调用：`POST /mcp`，Body 为 MCP JSON-RPC

### 5.4 新增一种定时任务

```java

@Component
@Slf4j
public class DemoCron {
    @Scheduled(cron = "0 0 1 * * ?")
    void dailyTask() {
        TraceUtil.resetTraceId();   // 重置 traceId，便于日志追踪
        log.info("每日任务开始");
    }
}
```

- 路径：`cn.hamm.spms.common.cron` 或业务模块下
- 入口必须 `TraceUtil.resetTraceId()`

### 5.5 新增一种 OAuth2 平台

```java
public enum OauthPlatform {
    GITEE(3, "gitee", GiteeCallback.class, "GITEE"),
}
```

- 实现 `AbstractOauthCallback.getUserInfo(code)` 与第三方通信，返回 `OauthUserInfo`

---

## 6. 数据库与 Schema

- **dev 模式**：`spring.jpa.hibernate.ddl-auto: create-drop`（自动建表 + 清空）
- **prod 模式**：`validate`（必须先手动初始化数据库）
- 表结构由 JPA 管理， **不依赖 Flyway / Liquibase**
- 生产部署前：导出 dev 数据库 → 在生产环境导入 → 切到 `validate`
- `init.lock` 文件存在则跳过初始化（dev 模式）

---

## 7. 配置文件优先级

`application.yml` < `application-{profile}.yml` < **环境变量 / 命令行参数**

默认激活的 Profile：`production`

---

## 8. 重要文件速查

| 文件                                                | 行号                                                             | 作用 |
|---------------------------------------------------|----------------------------------------------------------------|----|
| `SpmsApplication.java:34`                         | 启动成功标志输出                                                       |
| `SpmsDevData.java:88`                             | dev 模式初始化入口                                                    |
| `base/BaseService.java:33`                        | `beforeSaveToDatabase` 自动调 `CodeRuleService.fillFieldAutoCode` |
| `base/BaseService.java:44`                        | `publish()` 已 final，禁止重写                                       |
| `base/BaseController.java:34-54`                  | 发布后拒绝修改/删除                                                     |
| `base/BaseController.java:58`                     | `POST /publish` 接口                                             |
| `base/bill/AbstractBaseBillService.java`          | 单据状态机 / 钩子全集                                                   |
| `common/exception/CustomError.java`               | 自定义异常码（基址 200000）                                              |
| `common/AppConstant.java:12`                      | `BASE_CUSTOM_ERROR = 200000`                                   |
| `common/interceptor/RequestInterceptor.java:46`   | 超管判定 + 个人令牌校验                                                  |
| `module/system/coderule/enums/CodeRuleField.java` | 22 类业务编码枚举                                                     |
| `module/iot/report/ReportEventListener.java:31`   | MQTT 订阅启动入口                                                    |

---

## 9. 不要做的事

- ❌ 不要修改 AirPower 框架的依赖版本（由父 POM 锁定）
- ❌ 不要绕过 `XxxServices.getXxxService()` 直接 `@Autowired` 跨模块 Service（虽然可行，但破坏统一规范）
- ❌ 不要在 `pom.xml` 中引入新的第三方库，除非确有必要
- ❌ 不要删除或重命名 `BaseEntity` 中的 `isPublished` 字段
- ❌ 不要修改 `RequestInterceptor` 的核心权限逻辑
- ❌ 不要在 Service 中使用 `Thread.sleep` 或阻塞操作（用 `TaskUtil.run` 异步）
- ❌ 不要把业务异常信息硬编码在多处文案（统一到 `CustomError`）

---

## 10. 完成任务的验证清单

修改或新增任何代码后，自查：

- [ ] 是否使用了基类继承，没有绕开？
- [ ] 是否在模块的 `XxxServices.java` 注册了新 Service？
- [ ] 异常是否使用 `Errors.*` 静态工具，没有硬编码？
- [ ] Controller 是否标注了 `@Api` 和 `@Description`？
- [ ] 单据类是否实现了所有 `abstract` 状态方法？
- [ ] 字段是否都有 `@Description`？
- [ ] 字典字段是否都有 `@Dictionary`？
- [ ] 没有添加注释（除非用户明确要求）？
- [ ] 没有引入新的依赖（除非用户明确要求）？

---

## 11. 相关项目

- [AirPower4J](https://github.com/AirPowerTeam/AirPower4J) — 基础核心框架
- [SPMS-Docs](https://github.com/s-pms/SPMS-Docs) — 开发者指南（前端开发版）
- [SPMS-Docker](https://github.com/s-pms/SPMS-Docker) — Docker 一键部署
- [在线 Demo](https://spms.hamm.cn) — 项目示例站点（可能非最新代码）

---

## 12. 许可证

MIT License. 详见 [LICENSE](./LICENSE)。

联系方式：admin@hamm.cn
