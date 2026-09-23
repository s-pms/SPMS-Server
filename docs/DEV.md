# SPMS Server 开发指南

> 本文档面向开发者，详细说明 SPMS Server 的开发规范、目录结构、扩展点与常见任务的实现方式。
> 阅读本文前请先阅读 `../AGENTS.md` 了解项目定位。

---

## 1. 环境与工具

| 工具        | 版本              | 备注                                         |
|-------------|-------------------|----------------------------------------------|
| JDK         | 17+               | 必须，AirPower 父 POM 锁定                   |
| Maven       | 3.9+              | 项目自带 `mvnw`，也可使用本机 Maven          |
| MySQL       | 8.x               | 默认端口 3306，库名 `spms`，字符集 `utf8mb4` |
| Redis       | 任意稳定版        | 默认端口 6379                                |
| InfluxDB    | 6.5.0+            | 用于设备时序数据，可选                       |
| MQTT Broker | Mosquitto/EMQX 等 | 设备上报订阅，可选                           |

IDEA 安装 Lombok 插件后即可。

---

## 2. 启动项目

1. 克隆项目到本地并用 IDEA 打开
2. 等待 Maven 同步依赖（首次较慢）
3. 拷贝配置：

   ```bash
   cp src/main/resources/application-template.yml src/main/resources/application-local.yml
   ```

4. 编辑 `application-local.yml`，至少填入：

   ```yaml
   spring:
     datasource:
       url: "jdbc:mysql://localhost:3306/spms?allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&useSSL=false"
       username: "root"
       password: "<你的密码>"
   app:
     is-dev-mode: true        # 启用开发者模式，首次启动会执行初始化
   ```

5. IDEA 启动 `SpmsApplication`，Profile 选择 `local`
6. 控制台看到 `Hi Guy, SPMS is running at [8080] !` 即启动成功
7. 默认账号：`admin@hamm.cn / Aa123456`

> dev 模式会在运行目录生成 `init.lock` 文件，再次启动不会重复初始化。
> 删除该文件或修改 `ddl-auto: create-drop` 可触发重新初始化。

---

## 3. 目录结构与模块划分

```
cn.hamm.spms
├── SpmsApplication          # 入口
├── SpmsWebConfig            # Web & WebSocket 配置
├── SpmsDevData              # dev 模式数据初始化
├── base/                    # ⚠️ 业务模块不要修改，扩展通过继承
│   ├── BaseEntity/Service/Repository/Controller
│   └── bill/                # 单据抽象：AbstractBaseBillEntity + Service + Controller
├── common/                  # 应用层通用
│   ├── AppConfig / Configs  # app.* 配置 + 静态获取
│   ├── annotation/          # @AutoGenerateCode
│   ├── exception/           # CustomError（自定义异常码）
│   ├── influx/              # InfluxDB 助手
│   ├── interceptor/         # RequestInterceptor
│   └── cron/                # 定时任务
└── module/                  # 业务模块（每个模块含 XxxServices.java）
    ├── system / personnel / asset / channel
    ├── factory / mes / wms / iot / chat / open / mcp / wechat
```

---

## 4. 核心基类与扩展点

### 4.1 实体基类 `BaseEntity`

所有持久化实体必须继承自 `cn.hamm.spms.base.BaseEntity<E>`，特性：

- 字段 `isPublished`（已发布后禁止修改与删除，由 Controller 拦截）
- 默认 `@DynamicInsert` / `@DynamicUpdate`
- 字段需配合 Lombok `@Accessors(chain = true)` + `@Data`

```java

@Entity
@Table(name = "material")
@Description("物料")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@DynamicInsert
@DynamicUpdate
public class MaterialEntity extends BaseEntity<MaterialEntity> {
    // ...
}
```

### 4.2 Repository 基类 `BaseRepository`

```java

@Repository
public interface MaterialRepository extends BaseRepository<MaterialEntity> {
    // JPA 查询方法按 Spring Data 命名约定书写
    MaterialEntity getByCode(String code);
}
```

### 4.3 Service 基类 `BaseService<E, R>`

提供以下可重写钩子（默认空实现）：

| 方法                              | 用途                       |
|-----------------------------------|----------------------------|
| `beforeAppSaveToDatabase(entity)` | 入库前数据补全/校验        |
| `beforeAppUpdate(entity, exist)`  | 更新前业务校验             |
| `beforeAppDelete(entity)`         | 删除前业务校验             |
| `afterAppGet(entity)`             | 查询后置（脱敏、关联加载） |
| `afterAppAdd(id, source)`         | 新增后置                   |
| `afterAppUpdate(id, source)`      | 更新后置                   |
| `beforePublish(entity)`           | 发布前校验                 |

> `BaseService.beforeSaveToDatabase` 为 `final`，在内部自动调用
> `SystemServices.getCodeRuleService().fillFieldAutoCode(entity)`，
> 无需在子类重复实现。

### 4.4 Controller 基类 `BaseController<E, S, R>`

- 默认标注 `@Permission`（鉴权）
- 默认 `@Extends(exclude = {Export, QueryExport, Disable, Enable})` —— 关闭导出/启停
- 已 final 化 `beforeUpdate / beforeDelete`，已发布数据拒绝修改和删除
- 提供 `POST /publish` 接口
- 需要单独鉴权时，使用 `@Permission(authorize = false)` / `@Permission(login = false)`

---

## 5. 单据（Bill）开发规范

### 5.1 何时使用单据

凡是带明细、有状态流转（审核/驳回/完成）且涉及数量进度的业务模型：
生产计划 / 订单 / 领料单 / 入出库单 / 移库单 / 采购单 / 销售单。

### 5.2 单据四件套

| 角色       | 基类                                       |
|------------|--------------------------------------------|
| Entity     | `AbstractBaseBillEntity<E, D>`             |
| Service    | `AbstractBaseBillService<E, R, D, DS, DR>` |
| Controller | `BaseBillController<E, S, R, D, DS, DR>`   |
| Repository | `BaseBillRepository<E, D>`                 |

明细继承 `BaseBillDetailEntity<D>`，并实现抽象 `getQuantity/setQuantity/getFinishQuantity/setFinishQuantity`。

### 5.3 必填的 Service 抽象方法

```java
protected abstract IDictionary getAuditingStatus();        // 审核中

protected abstract IDictionary getAuditedStatus();         // 已审核

protected abstract IDictionary getRejectedStatus();        // 已驳回

public abstract IDictionary getBillDetailsFinishStatus();// 明细全完状态

public IDictionary getFinishedStatus();          // 单据完成态，可选
```

可重写的钩子：

| 钩子                                 | 用途                                     |
|--------------------------------------|------------------------------------------|
| `beforeAdd(bill)`                    | 新增前置（字段填充）                     |
| `beforeBillFinish(billId)`           | 单据完成前置                             |
| `afterBillFinished(billId)`          | 单据完成后置（联动其他单据）             |
| `afterAllBillDetailFinished(billId)` | 明细全完成后置（常用于联动创建入库单等） |
| `afterDetailFinishAdded(id, dfl)`    | 明细报工后置（更新库存、产量等）         |
| `afterBillAudited(billId)`           | 审核通过后置                             |
| `afterBillAdd(billId)`               | 单据新增后置                             |
| `afterBillUpdate(billId, src)`       | 单据更新后置                             |
| `getAutoAuditConfigFlag()`           | 返回 `ConfigFlag`，开启后新增自动审核    |

### 5.4 状态机

```
AUDITING ──audit──▶ AUDITED ──setBillDetailsAllFinished──▶ DETAILS_FINISHED ──setBillFinished──▶ DONE
   │                  ▲                                                       
   └──reject─► REJECTED ◀──canEdit── (回到驳回可再次编辑)
```

- `audit(billId)` 仅当状态 == `AUDITING` 才允许
- `reject(billId)` 仅当状态 == `AUDITING` 才允许，附带 `rejectReason`
- `canEdit(bill)` 仅当状态 == `REJECTED` 才允许（由 `BaseBillController.beforeAppUpdate` 拦截）
- `addDetailFinishQuantity(detail)` 内部事务包：扣减明细 + 后置钩子 + 全完判定

### 5.5 单据号自动生成

单据实体上的 `billCode` 字段加 `@AutoGenerateCode(CodeRuleField.OrderBillCode)`，
由 `BaseService.beforeSaveToDatabase` 自动填充。如需新增单据号字段类型：

1. 在 `cn.hamm.spms.module.system.coderule.enums.CodeRuleField` 追加枚举项
2. dev 模式重启即可自动建表与初始化默认前缀模板

---

## 6. 新建业务模块的流程（以 `asset.material` 为例）

### 6.1 五件套

```
module/<module>/<name>/
├── <Name>Entity.java
├── <Name>Service.java
├── <Name>Repository.java
├── <Name>Controller.java
└── enums/                       # 业务枚举（状态/类型）
    ├── <Name>Type.java
    └── <Name>Status.java
```

### 6.2 Entity

- 继承 `BaseEntity<E>`（普通）或 `AbstractBaseBillEntity<E, D>`（单据）
- 注解顺序：
  `@Entity @Table @Description @Data @EqualsAndHashCode(callSuper=true) @Accessors(chain=true) @DynamicInsert @DynamicUpdate`
- 字段必须含 `@Description`、必要时 `@Search`（可搜索）、`@Meta`（基础信息可序列化给前端）、`@ReadOnly`、
  `@Dictionary(EnumClass.class)`
- 关联关系使用 `jakarta.persistence.ManyToOne / ManyToMany`，配合 `FetchType.LAZY`

### 6.3 Service

```java

@Service
public class MaterialService extends BaseService<MaterialEntity, MaterialRepository> {
    @Override
    protected MaterialEntity beforeAppSaveToDatabase(@NotNull MaterialEntity material) {
        material.setPurchasePrice(Objects.requireNonNullElse(material.getPurchasePrice(), 0D));
        return material;
    }
}
```

### 6.4 Repository

```java

@Repository
public interface MaterialRepository extends BaseRepository<MaterialEntity> {
    MaterialEntity getByCode(String code);
}
```

### 6.5 Controller

```java

@Api("material")
@Description("物料")
@Extends({Curd.Export, Curd.QueryExport})   // 仅启用导出，其余沿用基类的 exclude
public class MaterialController
        extends BaseController<MaterialEntity, MaterialService, MaterialRepository> {
}
```

### 6.6 注册到服务定位器

在 `module/<module>/<Module>Services.java` 中追加：

```java

@Getter
private static MaterialService materialService;

@Autowired
private void initService(
        MaterialService materialService
        // others        
) {
    ModuleServices.materialService = materialService;
    // ...
}
```

跨模块调用：

```java
// SystemServices.getUserService().get(1L);
// WmsServices.getInputService().add();
```

### 6.7 菜单挂载（可选）

如果新模块需要前端菜单，进入 `cn.hamm.spms.module.system.menu.MenuService.initMenu()`，
按照现有结构追加一级 / 二级菜单。建议在首次启动后通过管理后台手动维护。

---

## 7. 异常处理

### 7.1 业务异常

继承自 `cn.hamm.airpower.core.exception.ServiceException`，推荐使用 `AirPower`
内置的静态错误工具：

```java
import static cn.hamm.airpower.exception.Errors.*;

//PARAM_INVALID.whenNull(obj, "对象不能为空");
//FORBIDDEN.when(bill.isPublished(), "已发布数据不允许编辑");
```

### 7.2 自定义异常枚举

在 `cn.hamm.spms.common.exception.CustomError` 中追加：

```java
//SMS_SEND_BUSY(102,"发送短信过于频繁，请稍后再试"),
```

- code 会自动加上 `AppConstant.BASE_CUSTOM_ERROR (200000)`，最终码为 `200102`
- 必须实现 `getKey()` / `getLabel()`（已在基类实现）

---

## 8. 鉴权与权限

### 8.1 权限注解

| 注解                             | 作用                                   |
|----------------------------------|----------------------------------------|
| `@Permission(login = false)`     | 放行登录（如 `/user/login`）           |
| `@Permission(authorize = false)` | 跳过权限校验（接口对所有登录用户开放） |
| `@Permission`                    | 走标准 RBAC 校验（默认）               |

> 默认所有 Controller 方法都会先在 `BaseController` 上叠加 `@Permission`，
> 因此未显式标注 `@Permission(authorize = false)` 的接口都会进行权限校验。

### 8.2 权限标识生成

- 普通 Controller 接口权限由 `AirPower` 通过 `@Description` + 路径自动扫描生成
- MCP 工具权限由 `McpService.getPermissionIdentity(McpTool)` 生成
- 通过 `super.service.getPermissionByIdentity(identity)` 反查
- 初始化逻辑：`PermissionService.loadPermission()` 在 dev 模式启动时自动加载

### 8.3 超级管理员

`UserEntity.isRootUser()` 当 `id == 1L` 视为超管，所有权限放行。
该判定在 `RequestInterceptor.checkUserPermission` 中硬编码。

---

## 9. IoT 设备数据上报

### 9.1 数据流

```
设备MQTT发布 (topic: sys/msg/v1, payload JSON)
   ↓
ReportEventListener.listen()  (应用启动后订阅)
   ↓
ReportMqCallback.messageArrived
   ├─ 解析 ReportData，根据 parameterCode 查 ParameterEntity
   ├─ Redis 缓存最新值：iot:device:<uuid>:code:<code> (TTL 5s)
   ├─ InfluxDB 持久化（measurement: iot:device:<code>, tag: uuid）
   ├─ 系统字段回写 DeviceEntity（status / alarm / partCount）
   └─ Redis 缓存全量报告：iot:device:<uuid>:report:  (供 getCurrentReport 查询)
```

### 9.2 内置系统参数

| 参数编码  | 标签     | 数据类型 | 含义         |
|-----------|----------|----------|--------------|
| `Status`  | 运行状态 | STATUS   | 设备运行状态 |
| `Alarm`   | 报警状态 | STATUS   | 报警状态     |
| `PartCnt` | 实时产量 | NUMBER   | 实时产量     |

新增自定义参数时，先在「参数管理」菜单中添加记录（设备上传的 `payload.code` 必须存在）。

### 9.3 设备接入

- 创建 `DeviceEntity`，填写 `code` / `name` / `uuid`，可关联 `ParameterEntity`
- `DeviceController.getDeviceConfig`（`POST /device/getDeviceConfig`）返回设备所需的参数列表与采集频率
- 设备端按 JSON `ReportData` 格式上报：

```json
{
  "deviceId": "<uuid>",
  "payloads": [
    {
      "code": "Status",
      "value": "1"
    },
    {
      "code": "PartCnt",
      "value": "1024"
    }
  ]
}
```

### 9.4 InfluxDB 配置

```yaml
app:
  influxdb:
    url: "http://127.0.0.1:8086"
    token: "<your-token>"
    org: "spms"
    bucket: "spms"
```

---

## 10. WebSocket（聊天）

### 10.1 通道与房间

- 服务端实现：`cn.hamm.spms.common.AppWebSocketHandler`
- 房间订阅：`subscribe(GROUP_PREFIX + roomId, session)` / `unsubscribe(...)`
- 在线列表：`ConcurrentHashMap<String, List<Long>> roomOnlineUserList`，发布消息时同时推送 `ONLINE_COUNT_CHANGED`

### 10.2 客户端事件类型

参见 `cn.hamm.spms.module.chat.enums.ChatEventType`：

- `ROOM_MEMBER_JOIN` / `ROOM_MEMBER_LEAVE`
- `ROOM_TEXT_MESSAGE`
- `ROOM_JOIN_FAIL` / `ROOM_JOIN_SUCCESS`
- `ROOM_LEAVE_SUCCESS`
- `ONLINE_COUNT_CHANGED`

---

## 11. OAuth2 接入

### 11.1 第三方平台

实现 `AbstractOauthCallback`，在 `OauthPlatform` 枚举中追加：

```java
//GITEE(3,"gitee",GiteeCallback .class, "GITEE"),
```

`AbstractOauthCallback.getUserInfo(code)` 负责与第三方通信并返回 `OauthUserInfo`。

### 11.2 Scope

- `BASIC_INFO`（默认）
- `CONTACT`（手机、邮箱）
- `PRIVACY`（性别、创建时间等）
- `REAL_NAME`（身份证、真实姓名）

---

## 12. MCP 工具开发

在任意 Service 方法上加 `@McpMethod`，会自动被 `McpService.scanMcpMethods` 扫描：

```java

@McpMethod(name = "查询物料", description = "按编码查询物料信息")
public MaterialEntity getMaterialByCode(@McpParam("编码") String code) {
    return repository.getByCode(code);
}
```

调用路径：`POST /mcp`，Body 为 `McpRequest`，遵循 MCP JSON-RPC 协议。
权限会自动注册到「MCP工具」菜单下，通过 RBAC 授权给指定角色。

---

## 13. 定时任务

在 `cn.hamm.spms.common.cron` 或业务模块下新建组件：

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

注意：

- 入口必须 `TraceUtil.resetTraceId()`，避免沿用请求上下文
- 任务类请保持无状态、不要在其中持有 Bean 引用之外的实例变量

---

## 14. 数据库与 Flyway

- 默认 JPA `ddl-auto: create-drop`（dev）/ `validate`（prod）
- 表名遵循 JPA 默认：类名小写，如 `material` / `device` / `order`（关键字 `orders`）
- 字段注释：`columnDefinition = "varchar(255) default '' comment '字段注释'"`
- 不依赖 Flyway/Liquibase，schema 由 JPA 管理
- 生产部署前导出一份建库 SQL，再切到 `validate`

---

## 15. 常用工具与配置

| 工具/位置                                        | 说明                                    |
|--------------------------------------------------|-----------------------------------------|
| `cn.hamm.airpower.core.Json`                     | JSON 序列化/反序列化                    |
| `cn.hamm.airpower.core.RandomUtil`               | 随机数 / 随机字符串                     |
| `cn.hamm.airpower.core.NumberUtil`               | 浮点加减乘除（避免精度丢失）            |
| `cn.hamm.airpower.core.DictionaryUtil`           | 字典枚举反查                            |
| `cn.hamm.airpower.core.TraceUtil`                | traceId 跟踪                            |
| `cn.hamm.airpower.core.ReflectUtil`              | 反射工具                                |
| `cn.hamm.airpower.core.TaskUtil.run`             | 异步执行任务                            |
| `cn.hamm.airpower.curd.helper.TransactionHelper` | 编程式事务                              |
| `cn.hamm.airpower.redis.RedisHelper`             | Redis 客户端（统一前缀 `spms:`）        |
| `cn.hamm.airpower.curd.config.CurdConfig`        | CRUD 全局开关                           |
| `cn.hamm.spms.common.Configs`                    | 静态持有 AirPower/Influx/WebSocket 配置 |

---

## 16. 代码规范

1. **包名**：`cn.hamm.spms.module.<module>.<business>`，枚举统一放在 `<business>.enums`
2. **类注释**：每个类顶部使用 `<h1>xxx</h1>` + `@author Hamm.cn` 风格
3. **Lombok**：必装，实体使用 `@Data + @EqualsAndHashCode(callSuper = true) + @Accessors(chain = true)`
4. **接口顺序**：所有方法按 public → protected → private 排序；Controller 公共方法先列出
5. **空行与格式**：方法体内各步骤之间使用空行分割，复杂逻辑加注释（除非要用户明确要求）
6. **命名**：
    - Entity 名 = 单数业务名词（`MaterialEntity`、`OrderEntity`）
    - 表名 = 实体名小写（`material`），关键字避开（如 `order` → `orders`）
    - Service / Controller 名同理
7. **空值/校验**：优先使用 `AirPower` 的 `Errors.*` 工具类进行断言
8. **异常**：自定义异常添加到 `CustomError`，避免散落硬编码
9. **接口设计**：Controller 中禁止直接 `try/catch` 业务异常；抛出让统一拦截器处理

---

## 17. 调试与排错

| 场景                            | 排查思路                                                                                  |
|---------------------------------|-------------------------------------------------------------------------------------------|
| 启动失败，提示 `init.lock` 存在 | 删除项目根目录下的 `init.lock` 文件，或确认 `ddl-auto` 是否为 `create-drop`               |
| 接口 401                        | 检查 `application.yml` 中 `airpower.api.access-token-secret` 是否被覆盖，Token 是否过期   |
| 接口 403                        | 权限不足；进入「权限管理」配置或通过 `getMyPermissionList` 接口调试                       |
| 单据状态卡住                    | 检查 `ConfigFlag.*AUTO_xxxx_AUDIT` 与 `getAutoAuditConfigFlag()` 是否匹配                 |
| InfluxDB 数据未落盘             | 确认 `app.influxdb.token/org/bucket` 配置正确，且 `getInfluxConfig()` 能成功返回非空      |
| MQTT 收不到数据                 | 确认应用启动时控制台日志 `SpmsApplication.main` 已成功执行 `reportEventListener.listen()` |
| 主键冲突                        | 检查 `ddl-auto`，避免 `validate` + `create` 混用导致表结构漂移                            |

---

## 18. 提交与版本管理

- 主分支：`master`
- 提交信息建议遵循 `feat:` / `fix:` / `refactor:` / `chore:` / `docs:` 前缀
- 修改 `../pom.xml` 版本号时同步更新 `airpower.version`（通常不动）
- 任何新增 `CodeRuleField`、`ConfigFlag`、`PermissionType` 必须提供默认值，避免老部署崩溃
- 与 AirPower 框架冲突时优先扩展而非覆盖；如确需覆盖请在 PR 中明确说明

---

## 19. 快速参考：模块的「最小可运行单元」

```java
// Entity
@Entity
@Table(name = "demo")
@Description("示例")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@DynamicInsert
@DynamicUpdate
public class DemoEntity extends BaseEntity<DemoEntity> {
    @Description("名称")
    @Column(columnDefinition = "varchar(255) default '' comment '名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class})
    @Meta
    @Search
    private String name;
}

// Repository
@Repository
public interface DemoRepository extends BaseRepository<DemoEntity> {
}

// Service
@Service
public class DemoService extends BaseService<DemoEntity, DemoRepository> {
}

// Controller
@Api("demo")
@Description("示例")
public class DemoController extends BaseController<DemoEntity, DemoService, DemoRepository> {
}

// 注册到服务定位器（DemoModuleServices.java 中）
@Getter
private static DemoService demoService;

@Autowired
private void initService(DemoService demoService) {
    DemoModuleServices.demoService = demoService;
}
```

启动应用 → 在浏览器或 Postman 测试 `POST /demo/add`，即完成最小闭环。

---

如有问题，请直接联系作者：admin@hamm.cn。