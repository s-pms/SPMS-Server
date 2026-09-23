# SPMS Server

> 智能生产管理系统（Smart Production Management System）后端项目

[![Core](https://svg.hamm.cn?key=Core&value=AirPower4J)](https://github.com/AirPowerTeam/AirPower4J)
[![Lang](https://svg.hamm.cn?key=Lang&value=Java17&bg=green)](https://www.java.com)
[![Base](https://svg.hamm.cn?key=Base&value=SpringBoot3)](https://spring.io/projects/spring-boot)
[![Data](https://svg.hamm.cn?key=Data&value=MySQL8)](https://www.mysql.com)
[![ORM](https://svg.hamm.cn?key=ORM.&value=JPA)](https://spring.io/projects/spring-data-jpa)

---

## 1. 项目定位

**SPMS Server** 是一个企业级智能生产管理系统的服务端。基于 **AirPower4J** 框架
（`cn.hamm:airpower:8.0.0`）构建，将 **MES / WMS / ERP / QMS / IoTS** 等核心生产管理模块
集成在同一个应用中，覆盖采购、生产、仓储、销售、设备物联等全链路业务。

主要面向：

- 智能制造、工业互联网、物联网行业的中小型生产制造企业
- 需要快速构建一站式生产管理平台的研发团队
- 学习现代 Java 企业级开发（Spring Boot 3 + JPA + AirPower4J）的开发者

---

## 2. 技术栈

| 类别          | 技术 / 版本                                              |
|---------------|----------------------------------------------------------|
| 语言 / 运行时 | Java 17                                                  |
| Web 框架      | Spring Boot 3（由 `cn.hamm:airpower:8.0.0` 父 POM 锁定） |
| ORM           | Spring Data JPA + Hibernate（`MySQL8Dialect`）           |
| 数据库        | MySQL 8                                                  |
| 缓存          | Redis（Spring Data Redis）                               |
| 时序数据库    | InfluxDB 6.5.0（设备数据上报）                           |
| 物联网        | Eclipse Paho MQTT（`org.eclipse.paho.client.mqttv3`）    |
| 实时通信      | WebSocket（基于 `airpower-websocket`，Redis 集群广播）   |
| 邮件          | Spring Mail + 阿里云企业邮箱（`smtp.exmail.qq.com`）     |
| AI 工具       | MCP（Model Context Protocol，`airpower-ai`）             |
| 构建          | Maven、Spring Boot Maven Plugin                          |
| 容器          | Docker（`amazoncorretto:17-alpine` 运行时）              |

---

## 3. 业务模块

> 每个业务模块都以 `cn.hamm.spms.module.<module>` 为根包，对应一个 `XxxServices`
> 服务定位类。

| 模块        | 包路径             | 主要业务                                                              |
|-------------|--------------------|-----------------------------------------------------------------------|
| `system`    | `module.system`    | 菜单 / 权限 / 编码规则 / 系统配置 / 单位 / 文件 / 索引 / 企业微信回调 |
| `personnel` | `module.personnel` | 用户（登录、验证码、个人令牌）/ 角色 / 部门                           |
| `asset`     | `module.asset`     | 物料 / 设备 / 合同（含合同参与方、附件）                              |
| `channel`   | `module.channel`   | 客户 / 供应商 / 采购 / 销售 / 采购单价 / 销售单价                     |
| `factory`   | `module.factory`   | 仓库（`Storage`） / 生产单元（`Structure`，树形结构）                 |
| `mes`       | `module.mes`       | 生产计划 / 生产订单 / 领料单 / BOM / 工序 / 工艺路线                  |
| `wms`       | `module.wms`       | 入库单 / 出库单 / 移库单 / 库存（均为单据模型）                       |
| `iot`       | `module.iot`       | 参数管理 + 设备数据上报（MQTT 订阅 + Redis 缓存 + InfluxDB 时序存储） |
| `chat`      | `module.chat`      | 房间（广场/官方房） + 成员 + WebSocket 实时聊天事件                   |
| `open`      | `module.open`      | OAuth2（含企业微信回调）/ 第三方登录 / 通知钩子 / 开放应用管理        |
| `mcp`       | `module.mcp`       | AI 工具（Model Context Protocol）注册中心                             |
| `wechat`    | `module.wechat`    | 微信相关（占位/扩展）                                                 |

---

## 4. 系统能力

- **RBAC 权限模型**：用户 ↔ 角色 ↔ 权限 + 菜单；超级管理员（`user.id = 1L`）放行所有
- **单据流引擎**：所有单据（计划 / 订单 / 入出库 / 移库 / 采购 / 销售）共用一套状态机
  `审核中 → 已驳回 → 准备中 → 执行中 → 已完成`
- **业务编码规则**：22 类业务编码（物料、设备、各类单据），支持 `[yyyy] [yy] [mm] [dd] [hh]` 模板及
  日 / 月 / 年三种流水号重置策略
- **系统配置中心**：`ConfigFlag` 驱动业务开关（如入库单自动审核、订单审核后自动开工等）
- **OAuth2 授权码模式**：内部应用直返 code、外部应用跳登录页确认，支持 `BASIC_INFO / CONTACT / PRIVACY / REAL_NAME` 四类
  scope
- **企业微信登录**：通过 `AbstractOauthCallback` 适配器模式支持扩展多平台
- **设备数据采集**：MQTT `sys/msg/v1` → Redis 缓存最新值 → InfluxDB 时序落盘；内置
  `Status / Alarm / PartCnt` 三个系统参数
- **AI / MCP 工具**：扫描 `cn.hamm.spms` 与 `cn.hamm.airpower` 包下带 `@McpMethod` 注解的方法作为工具注册
- **实时聊天**：WebSocket 房间模式（`AppWebSocketHandler`），支持公开房、密码房、官方房、热门房
- **代码自动生成**：单据号、合同号等字段通过 `@AutoGenerateCode(CodeRuleField)` 在 `beforeSaveToDatabase` 钩子中自动填充
- **发布锁定**：所有实体继承 `BaseEntity.isPublished`，已发布数据禁止修改和删除（由 `BaseController` 拦截）
- **数据脱敏**：`@Desensitize` 注解支持中文姓名、身份证号等自动脱敏
- **国际化字典**：所有枚举实现 `IDictionary`，支持通过 `DictionaryUtil.getDictionary` 反查
- **多端登录**：账号密码、邮箱验证码、第三方（OAuth2）

---

## 5. 项目结构

```
SPMS-Server/
├── pom.xml                              # 依赖 AirPower 父 POM + airpower-* 子模块
├── Dockerfile                           # 多阶段构建（maven → amazoncorretto:17-alpine）
├── deploy.sh                            # Linux jar 包热部署脚本
├── settings.xml                         # Maven 镜像（aliyun）
├── init.lock                            # 开发者模式初始化锁文件（运行后生成）
├── spms-server.log                      # 应用运行日志（运行后生成）
├── README.md
├── AGENTS.md                            # 本文件
├── docs/
│   ├── DEV.md                           # 开发指南
│   └── img/                             # README 截图
└── src/
    ├── main/
    │   ├── java/cn/hamm/spms/
    │   │   ├── SpmsApplication.java     # 入口（启用 WebSocket + Scheduling）
    │   │   ├── SpmsWebConfig.java       # Web/WebSocket 配置
    │   │   ├── SpmsDevData.java         # 开发者模式初始化 CommandLineRunner
    │   │   ├── base/                    # 实体/Service/Controller 基类
    │   │   │   ├── BaseEntity           # 含 isPublished
    │   │   │   ├── BaseRepository
    │   │   │   ├── BaseService          # CodeRule 自动填充 + publish
    │   │   │   ├── BaseController       # publish 接口 + 发布后拦截
    │   │   │   └── bill/                # 单据基类（Entity/Service/Controller）
    │   │   │       └── detail/          # 单据明细基类
    │   │   ├── common/                  # 应用通用
    │   │   │   ├── AppConstant.java
    │   │   │   ├── AppConfig.java       # app.* 配置
    │   │   │   ├── Configs.java         # 静态持有 AirPower Config + AppConfig
    │   │   │   ├── AppWebSocketHandler  # 房间聊天业务逻辑
    │   │   │   ├── annotation/          # @AutoGenerateCode
    │   │   │   ├── cron/                # @Scheduled 任务
    │   │   │   ├── exception/           # CustomError 自定义异常枚举
    │   │   │   ├── influx/              # InfluxDB 配置 + 助手
    │   │   │   └── interceptor/         # RequestInterceptor（鉴权 + 个人令牌校验）
    │   │   └── module/                  # 业务模块（详见第 3 节）
    │   └── resources/
    │       ├── application.yml                       # 主配置（生产）
    │       ├── application-template.yml              # 环境变量模板（无敏感信息）
    │       ├── application-production.yml            # 生产 Profile
    │       ├── application-local-hamm.yml             # 本地开发（gitignored）
    │       └── logback-spring.xml
    └── test/
        └── java/cn/hamm/spms/
```

---

## 6. 运行与部署

### 6.1 本地开发

1. 准备环境：JDK 17、Maven 3.9+、MySQL 8（已通过 `localhost:3306` 部署）、Redis（已通过
   `localhost:6379` 部署）、可选 InfluxDB、MQTT
2. 创建数据库：`CREATE DATABASE spms DEFAULT CHARSET utf8mb4;`
3. 拷贝配置：`cp src/main/resources/application-template.yml src/main/resources/application-local.yml`
   并填入实际连接（参考已有 `application-local-hamm.yml`）
4. 启动应用：使用 IDEA 加载 `pom.xml`，运行 `SpmsApplication`
    - `application-local.yml` 中打开 `app.is-dev-mode: true`
    - `spring.jpa.hibernate.ddl-auto: create-drop` 自动建表
5. 初始化完成后默认账号：`admin@hamm.cn / Aa123456`（ID 为 1，系统内置超级管理员）

### 6.2 生产部署

- 生产 Profile 下必须先手动初始化数据库，并将 `ddl-auto` 改为 `validate`
- Docker 一键体验请参考：[SPMS-Docker](https://github.com/s-pms/SPMS-Docker)
- 手动部署流程见 `deploy.sh`：清理旧进程 → 启动新 jar → 轮询 `/` 探活

### 6.3 配置文件优先级

`application.yml` < `application-{profile}.yml` < 环境变量 / 命令行参数

默认激活的 Profile：`production`

---

## 7. 关键设计点速览

| 关注点   | 实现                                                                                                                   |
|----------|------------------------------------------------------------------------------------------------------------------------|
| 依赖管理 | 继承 `cn.hamm:airpower:8.0.0`，所有 AirPower 子模块（curd/file/mqtt/ai/open/email/websocket）一站式引入                |
| 持久层   | JPA + Hibernate，使用 `@MappedSuperclass` 抽象公共字段，所有实体 `DynamicInsert/DynamicUpdate`                         |
| 事务     | `BaseBillService.addDetailFinishQuantity` 内部使用 `TransactionHelper.run` 包事务                                      |
| 缓存     | Redis `RedisHelper`，键统一前缀 `airpower.redis.prefix` (`spms:`) + 业务前缀                                           |
| 异步     | `TaskUtil.run(() -> ...)` 用于单据审核后异步触发后续钩子                                                               |
| 定时任务 | `@EnableScheduling` + `@Scheduled(cron = ...)`，注意每次任务入口重置 `TraceUtil.resetTraceId`                          |
| 服务定位 | 每个模块一个 `XxxServices` 静态服务定位器，跨模块调用 `SystemServices.getXxxService()`                                 |
| 异常体系 | 业务异常继承 `cn.hamm.airpower.core.exception.ServiceException`；自定义枚举码 `AppConstant.BASE_CUSTOM_ERROR = 200000` |
| 拦截器   | `RequestInterceptor` 扩展 `CurdRequestInterceptor`，实现 Token 校验 + 个人令牌禁用校验 + 权限白名单                    |
| 鉴权     | `@Permission(login = false)` 标注公开接口；`authorize = false` 跳过权限校验                                            |
| 接口过滤 | `@Extends({...})` 在 `BaseController/BaseBillController` 中默认 exclude `Export / Delete / Disable / Enable`           |

---

## 8. AI / 编辑器协作建议

如果你使用 AI 辅助开发（Cursor / Copilot / Claude 等）：

1. 在打开仓库前先阅读 `AGENTS.md`（本文档）与 `docs/DEV.md`
2. 修改前使用项目内的 `base/` 抽象基类扩展，不要绕过 `BaseService/BaseController` 自己写接口
3. 新增模块按 `module/<module>/<name>/` 结构，包含 `Entity / Service / Repository / Controller / enums/*` 五件套
4. 跨模块调用统一通过 `XxxServices.getXxxService()`
5. 所有枚举实现 `cn.hamm.airpower.core.interfaces.IDictionary`
6. 所有需要自动编号的字段使用 `@AutoGenerateCode(CodeRuleField.XXX)`，并在 `CodeRuleField` 追加新枚举项
7. 字典表字段使用 `@Dictionary(value = EnumClass.class, groups = {WhenAdd.class, WhenUpdate.class})` 标注
8. 任何状态变更逻辑优先在 `base/BaseService.beforeAppSaveToDatabase / beforeAppUpdate / beforePublish` 钩子中实现

---

## 9. 相关项目

- [AirPower4J](https://github.com/AirPowerTeam/AirPower4J) — 基础核心框架
- [SPMS-Docs](https://github.com/s-pms/SPMS-Docs) — 开发者指南（前端开发版）
- [SPMS-Docker](https://github.com/s-pms/SPMS-Docker) — Docker 一键部署
- [在线 Demo](https://spms.hamm.cn) — 项目示例站点（可能非最新代码）

---

## 10. 许可证

MIT License. 详见 `LICENSE`。

联系方式：admin@hamm.cn