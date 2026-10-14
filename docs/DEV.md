# SPMS Server 开发指南

> 本文档面向 **人类开发者**，系统性地介绍 SPMS Server 的开发规范、架构设计、模块扩展与常见任务的落地步骤。
> 如果你使用 AI 辅助开发，请同时让 AI 阅读 [AGENTS.md](../AGENTS.md)，保证 AI 输出与本项目风格一致。
> 如果你只是想快速体验项目，请阅读 [README.md](../README.md)。

---

## 目录

1. [快速入门](#1-快速入门)
2. [环境与工具](#2-环境与工具)
3. [项目结构](#3-项目结构)
4. [核心基类与扩展点](#4-核心基类与扩展点)
5. [单据（Bill）开发规范](#5-单据bill开发规范)
6. [新建业务模块实战](#6-新建业务模块实战)
7. [异常处理](#7-异常处理)
8. [鉴权与权限](#8-鉴权与权限)
9. [IoT 设备数据接入](#9-iot-设备数据接入)
10. [WebSocket 实时通信](#10-websocket-实时通信)
11. [OAuth2 第三方登录](#11-oauth2-第三方登录)
12. [MCP 工具开发](#12-mcp-工具开发)
13. [定时任务](#13-定时任务)
14. [数据库与迁移](#14-数据库与迁移)
15. [常用工具与配置](#15-常用工具与配置)
16. [代码规范](#16-代码规范)
17. [调试与排错](#17-调试与排错)
18. [测试与验证](#18-测试与验证)
19. [提交与版本管理](#19-提交与版本管理)
20. [模块最小可运行单元](#20-模块的最小可运行单元)

---

## 1. 快速入门

### 1.1 三步启动

```bash
# 1. 克隆代码
git clone https://github.com/s-pms/SPMS-Server.git
cd SPMS-Server

# 2. 创建数据库（MySQL 8）
mysql -uroot -p -e "CREATE DATABASE spms DEFAULT CHARSET utf8mb4;"

# 3. 拷贝配置并启动
cp src/main/resources/application-template.yml src/main/resources/application-local.yml
# 编辑 application-local.yml，至少填入 datasource + app.is-dev-mode: true
# 然后 IDEA 运行 SpmsApplication，Profile 选择 local
```

控制台看到 `Hi Guy, SPMS is running at [8080] !` 即启动成功。
首次启动会自动建表 + 初始化演示数据，生成 `init.lock` 文件。

### 1.2 默认账号

```
用户名：admin@hamm.cn
密  码：Aa123456
```

### 1.3 推荐学习路径

1. 阅读本文档 §3、§4，理解项目结构与基类设计
2. 跑通 §1.1 的启动流程，确认本地可访问
3. 阅读 §6，跟随示例完成一个最小业务模块
4. 阅读 §5，掌握单据开发（生产/订单/出入库）
5. 按需阅读 §9~§13，对应设备 / WebSocket / OAuth / MCP / 定时任务

---

## 2. 环境与工具

| 工具        | 版本                | 备注                                         |
|-------------|---------------------|----------------------------------------------|
| JDK         | 17+                 | 必须，AirPower 父 POM 锁定                   |
| Maven       | 3.9+                | 项目自带 `mvnw`，也可使用本机 Maven          |
| MySQL       | 8.x                 | 默认端口 3306，库名 `spms`，字符集 `utf8mb4` |
| Redis       | 任意稳定版          | 默认端口 6379                                |
| InfluxDB    | 6.5.0+              | 用于设备时序数据，**可选**                   |
| MQTT Broker | Mosquitto / EMQX 等 | 设备上报订阅，**可选**                       |
| IDEA        | 任意版本            | 需安装 Lombok 插件                           |

> ⚠️ 如果使用本地 Docker 已部署的 MySQL/Redis，可跳过本地安装步骤。

---

## 3. 项目结构

```
cn.hamm.spms
├── SpmsApplication          # 入口（@EnableWebSocket + @EnableScheduling）
├── SpmsWebConfig            # Web & WebSocket 配置
├── SpmsDevData              # dev 模式 CommandLineRunner 初始化数据
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

### 3.1 模块划分原则

| 包                                      | 职责                                                            |
|-----------------------------------------|-----------------------------------------------------------------|
| `base/`                                 | 框架基类，所有业务模块共享的抽象，**禁止修改**                  |
| `common/`                               | 横切关注点：配置、异常、拦截器、注解、cron                      |
| `module/<module>/<business>/`           | 业务实现，每个业务含 Entity/Service/Repository/Controller/enums |
| `module/<module>/<Module>Services.java` | 模块内所有 Service 的静态服务定位器                             |

### 3.2 服务定位器（重要约定）

每个模块都有一个 `XxxServices` 类，内部以静态字段持有本模块所有 Service，便于跨模块调用：

```java
public class WmsServices {
    @Getter
    private static InputService inputService;
    @Getter
    private static OutputService outputService;
    // ...

    @Autowired
    public void init(InputService inputService, OutputService outputService) {
        WmsServices.inputService = inputService;
        WmsServices.outputService = outputService;
    }
}
```

跨模块调用示例：

```java
WmsServices.getInputService().

add(inputBillEntity);
SystemServices.

getUserService().

get(1L);
```

> 优先使用服务定位器调用跨模块 Service，而非 `@Autowired`，保持调用风格统一。

---

## 4. 核心基类与扩展点

### 4.1 实体基类 `BaseEntity`

所有持久化实体必须继承自 `cn.hamm.spms.base.BaseEntity<E>`，关键特性：

- 字段 `isPublished`（已发布后禁止修改与删除，由 Controller 拦截）
- 默认 `@DynamicInsert / @DynamicUpdate`
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
    MaterialEntity getByCode(String code);
}
```

### 4.3 Service 基类 `BaseService<E, R>`

提供以下可重写钩子（默认空实现）：

| 方法                              | 用途                       |
|-----------------------------------|----------------------------|
| `beforeAppSaveToDatabase(entity)` | 入库前数据补全 / 校验      |
| `beforeAppUpdate(entity, exist)`  | 更新前业务校验             |
| `beforeAppDelete(entity)`         | 删除前业务校验             |
| `afterAppGet(entity)`             | 查询后置（脱敏、关联加载） |
| `afterAppAdd(id, source)`         | 新增后置                   |
| `afterAppUpdate(id, source)`      | 更新后置                   |
| `beforePublish(entity)`           | 发布前校验                 |

> `BaseService.beforeSaveToDatabase` 为 `final`，内部会自动调用
> `SystemServices.getCodeRuleService().fillFieldAutoCode(entity)`， **无需在子类重复实现**。

### 4.4 Controller 基类 `BaseController<E, S, R>`

- 默认标注 `@Permission`（鉴权）
- 默认 `@Extends(exclude = {Export, QueryExport, Disable, Enable})` —— 关闭导出 / 启停
- 已 `final` 化 `beforeUpdate / beforeDelete`，已发布数据拒绝修改和删除
- 提供 `POST /publish` 接口
- 需要单独鉴权时，使用 `@Permission(authorize = false)` / `@Permission(login = false)`

### 4.5 单据基类（详见 §5）

| 角色       | 基类                                       |
|------------|--------------------------------------------|
| Entity     | `AbstractBaseBillEntity<E, D>`             |
| Detail     | `BaseBillDetailEntity<D>`                  |
| Service    | `AbstractBaseBillService<E, R, D, DS, DR>` |
| Repository | `BaseBillRepository<E, D>`                 |
| Controller | `BaseBillController<E, S, R, D, DS, DR>`   |

---

## 5. 单据（Bill）开发规范

### 5.1 何时使用单据

凡是带明细、有状态流转（审核 / 驳回 / 完成）且涉及数量进度的业务模型：
生产计划 / 订单 / 领料单 / 入出库单 / 移库单 / 采购单 / 销售单。

### 5.2 单据号字段命名

单据主实体类需提供 `getBillCode()` 返回单据号字符串，由 `@AutoGenerateCode` 自动注入。

### 5.3 必填的 Service 抽象方法

```java
protected abstract IDictionary getAuditingStatus();        // 审核中

protected abstract IDictionary getAuditedStatus();         // 已审核

protected abstract IDictionary getRejectedStatus();        // 已驳回

public abstract IDictionary getBillDetailsFinishStatus();  // 明细全完状态

public IDictionary getFinishedStatus();                    // 单据完成态，可选
```

### 5.4 可重写的钩子

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

### 5.5 状态机

```
AUDITING ──audit──▶ AUDITED ──setBillDetailsAllFinished──▶ DETAILS_FINISHED ──setBillFinished──▶ DONE
   │                  ▲
   └──reject─► REJECTED ◀──canEdit── (回到驳回可再次编辑)
```

- `audit(billId)` 仅当状态 == `AUDITING` 才允许
- `reject(billId)` 仅当状态 == `AUDITING` 才允许，附带 `rejectReason`
- `canEdit(bill)` 仅当状态 == `REJECTED` 才允许（由 `BaseBillController.beforeAppUpdate` 拦截）
- `addDetailFinishQuantity(detail)` 内部事务包：扣减明细 + 后置钩子 + 全完判定

### 5.6 单据号自动生成

单据实体上的 `billCode` 字段加 `@AutoGenerateCode(CodeRuleField.OrderBillCode)`，
由 `BaseService.beforeSaveToDatabase` 自动填充。如需新增单据号字段类型：

1. 在 `cn.hamm.spms.module.system.coderule.enums.CodeRuleField` 追加枚举项
2. dev 模式重启即可自动建表与初始化默认前缀模板

### 5.7 一个完整的单据示例（简化）

```java
// 状态枚举
public enum OrderStatus implements IDictionary {
    AUDITING(1, "审核中"),
    AUDITED(2, "已审核"),
    REJECTED(3, "已驳回"),
    DETAILS_FINISHED(4, "明细已完"),
    DONE(5, "已完成");

    private final int code;
    private final String message;
}

// 单据 Service
@Service
public class OrderService extends AbstractBaseBillService<OrderEntity, OrderRepository, OrderDetailEntity, OrderDetailService, OrderDetailRepository> {

    @Override
    protected IDictionary getAuditingStatus() {
        return OrderStatus.AUDITING;
    }

    @Override
    protected IDictionary getAuditedStatus() {
        return OrderStatus.AUDITED;
    }

    @Override
    protected IDictionary getRejectedStatus() {
        return OrderStatus.REJECTED;
    }

    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return OrderStatus.DETAILS_FINISHED;
    }

    @Override
    protected void afterBillAudited(long billId) {
        log.info("订单审核通过，ID: {}", billId);
    }
}
```

---

## 6. 新建业务模块实战

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

### 6.2 Entity 编写

- 继承 `BaseEntity<E>`（普通）或 `AbstractBaseBillEntity<E, D>`（单据）
- 注解顺序：
  `@Entity @Table @Description @Data @EqualsAndHashCode(callSuper=true) @Accessors(chain=true) @DynamicInsert @DynamicUpdate`
- 字段必须含 `@Description`、必要时 `@Search`（可搜索）、`@Meta`（基础信息可序列化给前端）、`@ReadOnly`、
  `@Dictionary(EnumClass.class)`
- 关联关系使用 `jakarta.persistence.ManyToOne / ManyToMany`，配合 `FetchType.LAZY`

### 6.3 Service 编写

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

### 6.4 Repository 编写

```java

@Repository
public interface MaterialRepository extends BaseRepository<MaterialEntity> {
    MaterialEntity getByCode(String code);
}
```

### 6.5 Controller 编写

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
}
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

PARAM_INVALID.whenNull(obj, "对象不能为空");
FORBIDDEN.

when(bill.isPublished(), "已发布数据不允许编辑");
        FORBIDDEN.

show("直接抛错场景");
```

### 7.2 自定义异常枚举

在 `cn.hamm.spms.common.exception.CustomError` 中追加：

```java
SMS_SEND_BUSY(102,"发送短信过于频繁，请稍后再试"),
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

### 8.4 个人令牌

通过 `UserTokenType.PERSONAL` 颁发的 token 会在 `RequestInterceptor.getVerifiedToken`
中校验「存在性」与「启用状态」，失效 token 自动 401 / 403。

---

## 9. IoT 设备数据接入

### 9.1 数据流

```
设备 MQTT 发布 (topic: sys/msg/v1, payload JSON)
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

未配置时自动跳过持久化，但 Redis 缓存仍生效。

### 9.5 MQTT 配置

```yaml
airpower:
  mqtt:
    host: "127.0.0.1"
    port: 1883
    user: "<username>"
    pass: "<password>"
```

---

## 10. WebSocket 实时通信

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

### 10.3 Redis 集群广播

`airpower.websocket.support=redis` 启用 Redis pub/sub 跨实例广播，
`airpower.redis.prefix=spms:` 配置统一前缀。

---

## 11. OAuth2 第三方登录

### 11.1 第三方平台

实现 `AbstractOauthCallback`，在 `OauthPlatform` 枚举中追加：

```java
GITEE(3,"gitee",GiteeCallback .class, "GITEE"),
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

- 调用路径：`POST /mcp`，Body 为 `McpRequest`，遵循 MCP JSON-RPC 协议
- 权限会自动注册到「MCP工具」菜单下，通过 RBAC 授权给指定角色
- 扫描包：`cn.hamm.spms` + `cn.hamm.airpower`

---

## 13. 定时任务

在 `cn.hamm.spms.common.cron` 或业务模块下新建组件：

```java

@Component
@Slf4j
public class DemoCron {
    @Scheduled(cron = "0 0 1 * * ?")
    void dailyTask() {
        TraceUtil.resetTraceId();
        log.info("每日任务开始");
    }
}
```

注意：

- 入口必须 `TraceUtil.resetTraceId()`，避免沿用请求上下文
- 任务类请保持无状态、不要在其中持有 Bean 引用之外的实例变量

---

## 14. 数据库与迁移

- 默认 JPA `ddl-auto: create-drop`（dev）/ `validate`（prod）
- 表名遵循 JPA 默认：类名小写，如 `material` / `device` / `order`（关键字 `orders`）
- 字段注释：`columnDefinition = "varchar(255) default '' comment '字段注释'"`
- 不依赖 Flyway / Liquibase，schema 由 JPA 管理
- 生产部署前导出一份建库 SQL，再切到 `validate`

### 14.1 Dev 模式初始化

`init.lock` 文件存在则跳过初始化。如需重新初始化：

```bash
rm init.lock
# 或修改 ddl-auto 为 create-drop
```

---

## 15. 常用工具与配置

| 工具 / 位置                                      | 说明                                        |
|--------------------------------------------------|---------------------------------------------|
| `cn.hamm.airpower.core.Json`                     | JSON 序列化 / 反序列化                      |
| `cn.hamm.airpower.core.RandomUtil`               | 随机数 / 随机字符串                         |
| `cn.hamm.airpower.core.NumberUtil`               | 浮点加减乘除（避免精度丢失）                |
| `cn.hamm.airpower.core.DictionaryUtil`           | 字典枚举反查                                |
| `cn.hamm.airpower.core.TraceUtil`                | traceId 跟踪                                |
| `cn.hamm.airpower.core.ReflectUtil`              | 反射工具                                    |
| `cn.hamm.airpower.core.TaskUtil.run`             | 异步执行任务                                |
| `cn.hamm.airpower.curd.helper.TransactionHelper` | 编程式事务                                  |
| `cn.hamm.airpower.redis.RedisHelper`             | Redis 客户端（统一前缀 `spms:`）            |
| `cn.hamm.airpower.curd.config.CurdConfig`        | CRUD 全局开关                               |
| `cn.hamm.spms.common.Configs`                    | 静态持有 AirPower / Influx / WebSocket 配置 |

---

## 16. 代码规范

1. **包名**：`cn.hamm.spms.module.<module>.<business>`，枚举统一放在 `<business>.enums`
2. **类注释**：每个类顶部使用 `<h1>xxx</h1>` + `@author Hamm.cn` 风格
3. **Lombok**：必装，实体使用 `@Data + @EqualsAndHashCode(callSuper = true) + @Accessors(chain = true)`
4. **接口顺序**：所有方法按 public → protected → private 排序；Controller 公共方法先列出
5. **空行与格式**：方法体内各步骤之间使用空行分割，复杂逻辑加注释（除非用户明确要求）
6. **命名**：
    - Entity 名 = 单数业务名词（`MaterialEntity`、`OrderEntity`）
    - 表名 = 实体名小写（`material`），关键字避开（如 `order` → `orders`）
    - Service / Controller 名同理
7. **空值 / 校验**：优先使用 `AirPower` 的 `Errors.*` 工具类进行断言
8. **异常**：自定义异常添加到 `CustomError`，避免散落硬编码
9. **接口设计**：Controller 中禁止直接 `try/catch` 业务异常；抛出让统一拦截器处理
10. **持久化**：Jakarta EE 注解（`jakarta.persistence.*`），不要使用 `javax.persistence.*`

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
| Bean 循环引用                   | 检查 `XxxServices.init` 注入顺序，或排查构造函数注入的 Service 之间是否形成环             |

---

## 18. 测试与验证

### 18.1 单元测试

在 `src/test/java/cn/hamm/spms/` 下编写：

```java

@SpringBootTest
class MaterialServiceTest {
    @Autowired
    private MaterialService materialService;

    @Test
    void testAdd() {
        MaterialEntity material = new MaterialEntity().setName("测试物料");
        material = materialService.addAndGet(material);
        assertNotNull(material.getId());
    }
}
```

### 18.2 接口测试

启动应用后，可使用 Postman 测试：

```http
POST http://localhost:8080/user/login
Content-Type: application/json

{
  "email": "admin@hamm.cn",
  "password": "Aa123456"
}
```

复制返回的 `accessToken`，在后续接口 Header 中携带：

```
Authorization: Bearer <accessToken>
```

### 18.3 自动验证清单

每次提交前自查：

- [ ] 应用能否正常启动
- [ ] 新增模块的 `xx/add`、`xx/update`、`xx/get`、`xx/delete` 能否跑通
- [ ] 单据模块的「审核 → 报工 → 完成」能否跑通
- [ ] 无新增控制台异常 / 编译警告

---

## 19. 提交与版本管理

- 主分支：`master`
- 提交信息建议遵循 `feat:` / `fix:` / `refactor:` / `chore:` / `docs:` 前缀
- 修改 `../pom.xml` 版本号时同步更新 `airpower.version`（通常不动）
- 任何新增 `CodeRuleField`、`ConfigFlag`、`PermissionType` 必须提供默认值，避免老部署崩溃
- 与 AirPower 框架冲突时优先扩展而非覆盖；如确需覆盖请在 PR 中明确说明

---

## 20. 模块的最小可运行单元

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