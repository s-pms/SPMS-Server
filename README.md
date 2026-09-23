<div align="center">

<img src="https://cdn.hamm.cn/svg/spms/logo.svg" alt="SPMS Logo"/>

# S-PMS Server

**智能生产管理系统 · 后端服务（Smart Production Management System）**

[![Core](https://svg.hamm.cn?key=Core&value=AirPower4J)](https://github.com/AirPowerTeam/AirPower4J)
[![Lang](https://svg.hamm.cn?key=Lang&value=Java17&bg=green)](https://www.java.com)
[![Base](https://svg.hamm.cn?key=Base&value=SpringBoot3)](https://spring.io/projects/spring-boot)
[![Data](https://svg.hamm.cn?key=Data&value=MySQL8)](https://www.mysql.com)
[![ORM.](https://svg.hamm.cn?key=ORM.&value=JPA)](https://spring.io/projects/spring-data-jpa)
[![License](https://svg.hamm.cn?key=License&value=MIT&bg=red)](./LICENSE)

一个集成 **MES / WMS / ERP / QMS / IoT** 一体的企业级智能生产管理平台后端。

[在线 Demo](https://spms.hamm.cn) · [开发者指南](https://github.com/s-pms/SPMS-Docs) · [Docker 部署](https://github.com/s-pms/SPMS-Docker)

</div>

---

## ⚠️ 非常重要

> [!IMPORTANT]
>
> - 🔥 **请仔细阅读本文档了解项目后再进行下一步**
> - 🔥 **作者是为爱发电，没有过多精力进行无偿问题解答和技术支持**
> - 📖 使用 AI 辅助开发前，请让 AI 先阅读 [AGENTS.md](./AGENTS.md)，保证代码风格一致
> - 🌟 觉得项目不错？点击右上角 **Star** 支持一下！

---

## 📖 项目简介

**S-PMS（Smart Production Management System）** 是一个面向中小型制造企业的智能生产管理系统后端，基于 **AirPower4J**
框架构建，将多个核心生产管理模块一体化整合在同一个应用中：

| 模块    | 全称             | 核心职责                                         |
|---------|------------------|--------------------------------------------------|
| **MES** | 制造执行系统     | 车间生产活动优化管理，实时监控订单到成品的全流程 |
| **WMS** | 仓库管理系统     | 入库 / 出库 / 移库 / 库存的全流程管控            |
| **ERP** | 企业资源计划系统 | 整合财务、采购、销售、生产、库存等环节信息       |
| **QMS** | 质量管理系统     | 质量策划 / 控制 / 保证 / 改进的全周期管理        |
| **IoT** | 物联网管理系统   | 设备数据采集、远程监控、预防性维护               |

通过将这些系统一体化整合，帮助企业实现 **采购 → 生产 → 仓储 → 销售** 全流程的信息化、精细化、智能化管理。

---

## ✨ 核心特性

### 🏭 业务能力

- **单据流引擎**：所有单据（生产计划/订单/领料/入出库/移库/采购/销售）共享一套状态机（审核中 → 已驳回 → 准备中 → 执行中 →
  已完成）
- **业务编码规则**：22 类业务编码，支持 `[yyyy] [yy] [mm] [dd] [hh]` 模板及日/月/年三种流水号重置策略
- **RBAC 权限模型**：用户 ↔ 角色 ↔ 权限 ↔ 菜单，超管（`id=1L`）自动放行
- **生产执行闭环**：BOM → 工艺路线 → 生产订单 → 工序报工 → 成品入库，全链路贯通
- **价格管理**：物料多供应商采购价、多客户销售价统一管理

### 🤖 AI 与扩展

- **MCP 工具中心**：自动扫描 `@McpMethod` 注解方法，注册为 AI 可调用的工具
- **OAuth2 授权**：支持企业微信、Gitee 等多平台扩展（`AbstractOauthCallback` 适配器）

### 📡 IoT 集成

- **MQTT 设备接入**：订阅 `sys/msg/v1` 主题，自动解析设备上报
- **时序数据存储**：Redis 缓存最新值（5s TTL）+ InfluxDB 持久化
- **内置系统参数**：`Status / Alarm / PartCnt` 三个开箱即用的设备参数
- **设备配置下发**：`POST /device/getDeviceConfig` 返回设备所需的参数列表与采集频率

### 🛠️ 工程能力

- **发布锁定**：`isPublished` 字段，已发布数据自动禁止修改和删除
- **数据脱敏**：`@Desensitize` 注解支持中文姓名、身份证号等自动脱敏
- **国际化字典**：所有枚举实现 `IDictionary`，前端可反查标签
- **代码自动生成**：单据号、合同号等通过 `@AutoGenerateCode` 自动填充
- **多端登录**：账号密码、邮箱验证码、第三方 OAuth2

---

## 🧱 技术栈

| 类别          | 技术 / 版本                                                  |
|---------------|--------------------------------------------------------------|
| 语言 / 运行时 | **Java 17**                                                  |
| Web 框架      | **Spring Boot 3**（由 `cn.hamm:airpower:8.0.0` 父 POM 锁定） |
| ORM           | **Spring Data JPA + Hibernate**（MySQL8Dialect）             |
| 数据库        | **MySQL 8**                                                  |
| 缓存          | **Redis**（Spring Data Redis，统一前缀 `spms:`）             |
| 时序数据库    | **InfluxDB 6.5.0**（设备数据上报）                           |
| 物联网        | **Eclipse Paho MQTT**                                        |
| 实时通信      | **WebSocket**（基于 `airpower-websocket`，Redis 集群广播）   |
| 邮件          | **Spring Mail** + 阿里云企业邮箱                             |
| AI 工具       | **MCP**（Model Context Protocol，`airpower-ai`）             |
| 构建          | **Maven**、Spring Boot Maven Plugin                          |
| 容器          | **Docker**（`amazoncorretto:17-alpine`）                     |

---

## 📦 业务模块

> 每个业务模块都以 `cn.hamm.spms.module.<module>` 为根包，对应一个 `XxxServices` 服务定位类。

| 模块        | 包路径             | 主要业务                                                              |
|-------------|--------------------|-----------------------------------------------------------------------|
| `system`    | `module.system`    | 菜单 / 权限 / 编码规则 / 系统配置 / 单位 / 文件 / 索引 / 企业微信回调 |
| `personnel` | `module.personnel` | 用户（登录、验证码、个人令牌）/ 角色 / 部门                           |
| `asset`     | `module.asset`     | 物料 / 设备 / 合同（含合同参与方、附件）                              |
| `channel`   | `module.channel`   | 客户 / 供应商 / 采购 / 销售 / 采购单价 / 销售单价                     |
| `factory`   | `module.factory`   | 仓库（Storage）/ 生产单元（Structure，树形结构）                      |
| `mes`       | `module.mes`       | 生产计划 / 生产订单 / 领料单 / BOM / 工序 / 工艺路线                  |
| `wms`       | `module.wms`       | 入库单 / 出库单 / 移库单 / 库存（均为单据模型）                       |
| `iot`       | `module.iot`       | 参数管理 + 设备数据上报（MQTT + Redis + InfluxDB）                    |
| `chat`      | `module.chat`      | 房间（广场/官方房）+ 成员 + WebSocket 实时聊天事件                    |
| `open`      | `module.open`      | OAuth2 / 第三方登录 / 通知钩子 / 开放应用管理                         |
| `mcp`       | `module.mcp`       | AI 工具（MCP）注册中心                                                |
| `wechat`    | `module.wechat`    | 微信相关（占位/扩展）                                                 |

---

## 🏗️ 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                       客户端 / 第三方应用                       │
│    Web SPA · Mobile App · 设备 MQTT 客户端 · OAuth2 回调       │
└──────────────────┬───────────────────────────────┬───────────┘
                   │ HTTP/JSON                     │ MQTT
                   │ WebSocket                     │
┌──────────────────▼───────────────────────────────▼───────────┐
│                  SPMS Server（本项目）                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Controller 层（@Permission RBAC + Extends 过滤）     │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │  Service 层（BaseService 钩子 + 单据状态机）          │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │  Repository 层（JPA + Hibernate · MySQL8Dialect）    │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────┬──────────┬──────────┬──────────┬────────────┐ │
│  │ 系统模块 │ 人员模块 │ 资产模块 │ 渠道模块 │ MES/WMS/...│ │
│  └──────────┴──────────┴──────────┴──────────┴────────────┘ │
└────┬────────────┬────────────┬─────────────┬────────────────┘
     │            │            │             │
┌────▼────┐  ┌────▼────┐  ┌────▼────┐   ┌───▼─────┐
│  MySQL  │  │  Redis  │  │InfluxDB │   │  MQTT   │
│   主库  │  │ 缓存集群 │  │ 时序数据 │   │ Broker  │
└─────────┘  └─────────┘  └─────────┘   └─────────┘
```

---

## 🖼️ 产品截图

<p align="center">
<img src="/docs/img/1.png" width="24%"/>
<img src="/docs/img/2.png" width="24%"/>
<img src="/docs/img/3.png" width="24%"/>
<img src="/docs/img/4.png" width="24%"/>
<br/>
<img src="/docs/img/5.png" width="24%"/>
<img src="/docs/img/6.png" width="24%"/>
<img src="/docs/img/7.png" width="24%"/>
<img src="/docs/img/8.png" width="24%"/>
<br/>
<img src="/docs/img/9.png" width="24%"/>
<img src="/docs/img/10.png" width="24%"/>
<img src="/docs/img/11.png" width="24%"/>
<img src="/docs/img/12.png" width="24%"/>
<br/>
<img src="/docs/img/13.png" width="24%"/>
<img src="/docs/img/14.png" width="24%"/>
<img src="/docs/img/15.png" width="24%"/>
<img src="/docs/img/16.png" width="24%"/>
<br/>
<img src="/docs/img/17.png" width="24%"/>
<img src="/docs/img/18.png" width="24%"/>
<img src="/docs/img/19.png" width="24%"/>
<img src="/docs/img/20.png" width="24%"/>
</p>

---

## 🚀 快速开始

### 方式一：Docker 一键部署（推荐）

查看我们的 Docker 一键部署仓库：[SPMS-Docker](https://github.com/s-pms/SPMS-Docker)，最快速体验完整服务。

### 方式二：本地源码运行

#### 技能前提

需要熟悉以下技术： **Java 17** · **Spring Boot 3** · **Maven** · **IDEA** · **MySQL** · **Redis**

#### 1. 创建数据库

```sql
CREATE DATABASE spms DEFAULT CHARSET utf8mb4;
```

#### 2. 克隆源代码

```shell
# Github
git clone https://github.com/s-pms/SPMS-Server.git

# 或 Gitee（国内推荐）
git clone https://gitee.com/s-pms/SPMS-Server.git
```

#### 3. 初始化配置

```bash
cp src/main/resources/application-template.yml src/main/resources/application-local.yml
```

编辑 `application-local.yml`，至少填入：

```yaml
spring:
  datasource:
    url: "jdbc:mysql://localhost:3306/spms?allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&useSSL=false"
    username: "root"
    password: "<你的密码>"
app:
  is-dev-mode: true        # 启用开发者模式，首次启动自动初始化
```

#### 4. 启动应用

- IDEA 打开 `pom.xml`，等待 Maven 依赖同步
- 运行 `cn.hamm.spms.SpmsApplication`，Profile 选择 `local`
- 控制台看到 `Hi Guy, SPMS is running at [8080] !` 即启动成功
- 自动建表（`ddl-auto: create-drop`），初始化完成后生成 `init.lock`

#### 5. 默认账号

```
用户名：admin@hamm.cn
密  码：Aa123456
```

> 系统内置超级管理员（`id = 1`），拥有所有权限。

#### 6. 数据库怎么办？

使用 **JPA** 自动管理，无需手动建表。`ddl-auto: create-drop` 自动建表。 **生产部署前**：导出数据库结构 → 修改
`ddl-auto: validate` → 重新导入。

---

## 📚 文档导航

| 文档                                                | 适合谁                                         | 内容                                       |
|-----------------------------------------------------|------------------------------------------------|--------------------------------------------|
| [README.md](./README.md)                            | GitHub 访客                                    | 项目概览、快速体验                         |
| [AGENTS.md](./AGENTS.md)                            | AI Agent（Copilot/Cursor/Claude/Qwen Code 等） | 开发规范、目录结构、编码风格约定           |
| [docs/DEV.md](./docs/DEV.md)                        | 人类开发者                                     | 完整开发指南、单据开发、IoT 接入、调试排错 |
| [SPMS-Docs](https://github.com/s-pms/SPMS-Docs)     | 前端开发者                                     | 配套前端开发指南                           |
| [SPMS-Docker](https://github.com/s-pms/SPMS-Docker) | 运维 / 部署者                                  | Docker 一键部署方案                        |

---

## 🤝 贡献指南

欢迎各种形式的贡献：

- 🐛 **报告 Bug**：提交 [Issue](https://github.com/s-pms/SPMS-Server/issues)
- 💡 **功能建议**：告诉我们你的需求场景
- 🔧 **提交代码**：Fork → 修改 → Pull Request
- 📖 **完善文档**：文档同样重要

### 提交规范

```
feat: 新增 xxx 功能
fix: 修复 xxx 问题
refactor: 重构 xxx 模块
docs: 更新文档
chore: 杂项修改（构建/工具/配置）
```

---

## 💬 社区交流

- **QQ 群**：773726377（智能制造、工业互联网、物联网交流）
- **在线 Demo**：[https://spms.hamm.cn](https://spms.hamm.cn)

> ⚠️ 示例项目可能不是最新代码，建议自行部署后体验。

---

## 🌟 相关项目

- **[AirPower4J](https://github.com/AirPowerTeam/AirPower4J)** — 基础核心框架
- **[SPMS-Docs](https://github.com/s-pms/SPMS-Docs)** — 开发者指南
- **[SPMS-Docker](https://github.com/s-pms/SPMS-Docker)** — Docker 一键部署
- **[在线 Demo](https://spms.hamm.cn)** — 项目示例站点

如果本项目对你有帮助，欢迎给 **AirPower4J** 也点一个 ⭐️，基础框架的迭代速度决定了这个项目的上限。

---

## 📜 许可证

本项目基于 [MIT License](./LICENSE) 开源，你可以放心使用于商业项目。

---

## 📮 联系我们

- **作者邮箱**：admin@hamm.cn
- **GitHub**：[s-pms/SPMS-Server](https://github.com/s-pms/SPMS-Server)
- **Gitee**：[s-pms/SPMS-Server](https://gitee.com/s-pms/SPMS-Server)

---

<div align="center">

**如果觉得项目对你有帮助，请点一个 ⭐️ Star 支持我们！**

Made with ❤️ by [Hamm](https://github.com/s-pms)

</div>
