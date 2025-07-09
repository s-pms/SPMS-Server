<p align="center">
<img src="https://cdn.hamm.cn/svg/spms/logo.svg"/>
</p>

<p align="center">
  <img src="https://svg.hamm.cn?key=Core&value=AirPower4J"/>
  <img src="https://svg.hamm.cn?key=Lang&value=Java17&bg=green"/>
  <img src="https://svg.hamm.cn?key=Base&value=SpringBoot3"/>
  <img src="https://svg.hamm.cn?key=Data&value=MySQL8"/>
  <img src="https://svg.hamm.cn?key=ORM.&value=JPA"/>
</p>
<p align="center">
<a href="https://github.com/s-pms/SPMS-Server">@Github</a> <a href="https://gitee.com/s-pms/SPMS-Server">@Gitee</a>
</p>

## 在线交流群

我们开通了QQ群 **773726377**，如果你对本项目有任何疑问，或者是在智能制造、工业互联网、物联网行业进行交流，欢迎加入我们的交流群。

## S-PMS介绍（后端项目）

**S-PMS** `(Smart Production Management System)` **智能生产管理系统** ，是一个集成化、智能化的企业级应用软件，它集成了多个核心的生产管理模块，包括
**制造执行系统** `(MES, Manufacturing Execution System)`、**仓库管理系统** `(WMS, Warehouse Management System)`、*
*企业资源计划系统** `(ERP, Enterprise Resource Planning)`、**质量管理系统** `(QMS, Quality Management System)` 以及 *
*物联网管理系统** `(IoTS,Internet of Things System)` 等。

- **MES (Manufacturing Execution System) 制造执行系统**
  ，主要用于车间层的生产活动优化管理，实时监控从订单下达到产品完成的整个生产过程，实现对生产数据的实时采集和分析，以提高生产效率、产品质量和资源利用率。

- **WMS (Warehouse Management System) 仓库管理系统**，用于全面管理和控制仓库业务流程，包括入库、出库、库存、盘点、批次管理等，实现仓库作业高效、准确、透明。

- **ERP (Enterprise Resource Planning) 企业资源计划系统**，整合了企业的所有资源，包括财务、采购、销售、生产、库存等各个环节的信息，为企业决策提供及时、准确的数据支持。

- **QMS (Quality Management System) 质量管理系统**，主要用于质量管理与控制，确保产品和服务满足规定以及客户期望的质量要求，包括质量策划、质量控制、质量保证及质量改进等活动。

- **IoTS（Internet of Things System) 物联网管理系统**，通过连接各种生产设备和传感器，收集海量实时数据，实现设备远程监控、预防性维护、生产过程智能化控制等功能。

通过将这些系统功能一体化整合在 **S-PMS** 中，企业可以实现从采购、生产、仓储到销售全流程的信息化、精细化、智能化管理，有效提升企业的整体运营效率和市场竞争力。

## 产品截图

<p align="center">
<img src="/docs/img/1.png" width="18%"/>
<img src="/docs/img/2.png" width="18%"/>
<img src="/docs/img/3.png" width="18%"/>
<img src="/docs/img/4.png" width="18%"/>
<img src="/docs/img/5.png" width="18%"/>
<img src="/docs/img/6.png" width="18%"/>
<img src="/docs/img/7.png" width="18%"/>
<img src="/docs/img/8.png" width="18%"/>
<img src="/docs/img/9.png" width="18%"/>
<img src="/docs/img/10.png" width="18%"/>
<img src="/docs/img/11.png" width="18%"/>
<img src="/docs/img/12.png" width="18%"/>
<img src="/docs/img/13.png" width="18%"/>
<img src="/docs/img/14.png" width="18%"/>
<img src="/docs/img/15.png" width="18%"/>
<img src="/docs/img/16.png" width="18%"/>
<img src="/docs/img/17.png" width="18%"/>
<img src="/docs/img/18.png" width="18%"/>
<img src="/docs/img/19.png" width="18%"/>
<img src="/docs/img/20.png" width="18%"/>
</p>

## 开发者指南

本仓库为 **S-PMS**
Server后端项目仓库，你可以查看 [开发者指南@Github](https://github.com/s-pms/SPMS-Docs)、[开发者指南@Gitee](https://gitee.com/s-pms/SPMS-Docs)
等帮助文档。

## 快速入门

这是
**`AirPower4J`**
（[Github](https://github.com/AirPowerTeam/AirPower4J)/[Gitee](https://gitee.com/air-power/AirPower4J)）
的宿主项目，你可以使用下面的方式进行使用：

### 创建项目文件夹

创建项目文件夹， 如 `SPMS`，然后使用 **Git** 将源代码 clone 至此目录。

### 下载源代码

- 通过 **Github** 代码仓库初始化

  ```shell
  git clone https://github.com/s-pms/SPMS-Server.git
  ```

- 通过 **Gitee** 代码仓库初始化(推荐)

  ```shell
  git clone https://gitee.com/s-pms/SPMS-Server.git
  ```

### 运行项目

- 加载依赖

  > 使用 IDEA 打开 `SPMS-Server` 目录，刷新项目的 `maven` 依赖，等待依赖安装完成即可。

- 修改环境变量

  > 复制 `resources/application-template.yml` 为你运行环境的配置文件，如 `application-local.yml`
  ，然后使用这个配置文件启动即可。（IDEA编辑启动有效配置文件为对应的 `local`）

- 无数据库脚本?

  > 我们使用了 **JPA** 进行自动数据库操作，环境变量配置文件中的 `ddl-auto: create-drop`
  即可自动完成。如果你测试完毕计划部署生产，可以将测试完毕之后的数据库导出，然后在配置文件中修改 `ddl-auto: validate`
  ，再将导出的数据库文件重新导入数据库即可。

## 非常重要

> [!IMPORTANT]
>
> **🔥 请仔细阅读本文档了解项目后再进行下一步。**
>
> **🔥 作者是为爱发电，没有过多精力进行无偿问题解答和技术支持。**

## 联系我们

**S-PMS** 所有代码均在 **MIT** 开源协议规范下免费提供，你可以放心使用。

如果有定制需求，欢迎联系我们：

Email: admin@hamm.cn

