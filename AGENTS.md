# SPMS-Server OpenCode 指南

> OpenCode 会话专属的精简指南。单一文件即可完成 ramp-up。

## 1. 项目一句话

基于 **Spring Boot 3 + JPA + MySQL + Redis + MQTT + InfluxDB** 的智能生产管理后端（MES/WMS/ERP/IoT），强依赖 [
`cn.hamm.airpower`](https://github.com/AirPowerTeam/AirPower4J) 6.5.0 父 POM 提供的 CURD / WebSocket / MCP / 字典 /
异常体系。单 Maven 模块，无 monorepo、无 submodule、无 CI。

## 2. 技术栈

| 技术                    | 版本            | 说明                                             |
|-------------------------|-----------------|--------------------------------------------------|
| Java                    | 17              | 已通过 `pom.xml` 父 POM 锁定                     |
| Spring Boot             | 3.x             | 由 airpower 父 POM 带入                          |
| Maven                   | 3.9+            | 使用 `./mvnw`（已含 wrapper），勿依赖系统 `mvn`  |
| AirPower                | 6.5.0           | 父 POM，几乎所有能力都从 `cn.hamm.airpower.*` 取 |
| JPA/Hibernate           | Spring Data JPA | `ddl-auto` 见 §4 "环境"                          |
| MySQL                   | 8.x             | 库名 `spms`                                      |
| Redis / MQTT / InfluxDB | -               | 缓存 / 物联网上行 / 时序数据                     |

## 3. 常用命令

```bash
./mvnw -s settings.xml clean package -DskipTests   # 编译 / 打包（与 Dockerfile 完全一致）
./mvnw test                                         # 注意：依赖 local-hamm profile，见 §5
./mvnw spring-boot:run -Dspring-boot.run.profiles=local-hamm   # 本地启动
docker build -t spms-server .                       # Dockerfile 已含 COPY settings.xml
./deploy.sh                                          # 仅在生产环境（/home/server/），注意 JAR 名见 §13
```

## 4. 环境与 Profile（启动前必读）

- **本地 profile 配置**：复制 `src/main/resources/application-template.yml` 为 `application-local-<your-name>.yml`，按需填入
  DB/Redis/MQTT/InfluxDB/OSS/邮箱密钥。`application-local-*.yml` 已在 `.gitignore`， **不要**提交个人密钥。
    - 例外：`application-local-hamm.yml` 是仓库自带的 hamm 本地配置，被误提交——保留即可，但其中含明文邮箱密钥、AI
      Key、企业微信密钥， **不要复制粘贴其中密钥**。
- **默认 profile**：`application.yml` 中 `spring.profiles.active=production`；开发时务必显式切换到 `local-*`，否则会用
  `production` profile 去连线上 MySQL/Redis。
- **ddl-auto 行为差异**：
    - `application.yml`（基线）→ `validate`
    - `application-production.yml` → `update`（profile 覆盖基线，生产实际是 update）
    - `application-local-hamm.yml` → `create-drop`（每次重启清表）
    - `application-template.yml` → `update`
- **依赖服务**：`localhost:3306` MySQL（库名 `spms`）、`localhost:6379` Redis、MQTT broker、InfluxDB。MQTT/InfluxDB 不在
  docker-compose 默认栈里，需自行准备。
- **端口**：`8080`（见 `application.yml`）。

## 5. 测试与数据初始化

- **测试目录目前只有一个**：`src/test/java/cn/hamm/spms/ApplicationTest.java`。它使用 `@ActiveProfiles("local-hamm")` +
  `RedisHelper`，所以：
    - 跑 `./mvnw test` **必须** 保证 `application-local-hamm.yml` 在 classpath（默认就在 `src/main/resources/` 下）。
    - 跑测试需要本地 Redis 在线。
- **`SpmsDevData`**：实现 `CommandLineRunner`，仅当 `app.is-dev-mode: true` 且（无 `init.lock` 或
  `ddl-auto=create-drop`）时执行种子数据（用户、权限、菜单、demo 物料/仓库/BOM 等）。`init.lock` 写在 **仓库根目录**，已被
  `.gitignore`；首次启动后即存在，再次启动不会重跑种子。删 `init.lock` + 重启可强制重跑。

## 6. 仓库目录结构

```
src/main/java/cn/hamm/spms/
├── Application.java              # 启动类（cn.hamm.spms.SpmsApplication），含 MQTT report listener 初始化
├── DevDataInitRunner.java        # 种子数据
├── WebConfig.java                # WebSocket / 拦截器 / 过滤器注册
├── base/                         # BaseEntity / BaseService / BaseRepository / BaseController + bill/
├── common/                       # Configs / AppConfig / AppWebSocketHandler / aliyun / influx / cron / interceptor ...
└── module/                       # 业务模块根
    ├── asset/  channel/  chat/   factory/  iot/  mcp/
    ├── mes/    open/    personnel/  system/  wechat/  wms/

src/main/resources/
├── application.yml               # 基线配置（active=production，ddl-auto=validate）
├── application-template.yml      # 本地 profile 模板（gitignore 之外、用于复制）
├── application-production.yml    # 生产 profile（覆盖 ddl-auto=update）
├── application-local-hamm.yml    # hamm 本地配置（已误提交，含明文密钥）
├── logback-spring.xml
└── templates/
```

业务模块下的 **四件套**（如 `module/wms/inventory/`）：

```
function/
├── enums/                # 枚举（可选），命名 XXXType / XXXStatus
├── XxxEntity.java        # 大驼峰 + Entity 后缀
├── XxxRepository.java    # 大驼峰 + Repository 后缀
├── XxxService.java       # 大驼峰 + Service 后缀
└── XxxController.java    # 大驼峰 + Controller 后缀
```

命名规范：

| 层次        | 命名           | 示例                                        |
|-------------|----------------|---------------------------------------------|
| 包名        | 全小写英文单词 | `inventory`、`input`、`output`              |
| 类          | 大驼峰 + 后缀  | `InventoryEntity`                           |
| 方法 / 变量 | 小驼峰         | `getByMaterialIdAndStorageId`、`materialId` |
| 常量        | 全大写下划线   | `DEFAULT_PAGE_SIZE`                         |

模板示例见 `cn.hamm.spms.module.wms.inventory`、`cn.hamm.spms.module.factory.storage`。

## 7. 继承体系

所有业务类继承项目提供的基类（位于 `cn.hamm.spms.base`）：

| 层次       | 基类                                                                |
|------------|---------------------------------------------------------------------|
| Entity     | `BaseEntity<E extends BaseEntity<E>>`                               |
| Repository | `BaseRepository<E extends BaseEntity<E>>`                           |
| Service    | `BaseService<E extends BaseEntity<E>, R extends BaseRepository<E>>` |
| Controller | `BaseController<E, S, R>`                                           |

单据类（bill/）走 `AbstractBaseBillEntity` + `AbstractBaseBillService` + `BaseBillController` + `IBaseBillAction`。

## 8. 编码风格

### 8.1 文件头注释（强制）

```java
/**
 * <h1>类描述</h1>
 *
 * @author Hamm.cn
 */
```

### 8.2 实体类注解顺序（固定）

```java

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "table_name")
@Description("描述")
public class XxxEntity extends BaseEntity<XxxEntity> {
}
```

### 8.3 Controller 注解顺序

```java

@Api("api_path")
@Description("功能描述")
@Extends({GetDetail, GetPage})   // 常用：GetDetail / GetPage / Add / Update / Delete
public class XxxController extends BaseController<XxxEntity, XxxService, XxxRepository> {
}
```

**不要自己写方法**，用 `@Extends` 暴露 AirPower 的 CURD 接口。

### 8.4 Service / Repository

```java

@Service
public class XxxService extends BaseService<XxxEntity, XxxRepository> {
}

@Repository
public interface XxxRepository extends BaseRepository<XxxEntity> {
}
```

### 8.5 字段定义

```java

@Description("物料信息")
@ManyToOne(fetch = EAGER)
private MaterialEntity material;

@Description("库存数量")
@Column(columnDefinition = "double(20, 6) UNSIGNED default 0 comment '库存数量'")
private Double quantity;

@Description("存储类型")
@Column(columnDefinition = "int UNSIGNED default 1 comment '存储类型'")
@Dictionary(value = InventoryType.class, groups = {WhenAdd.class, WhenUpdate.class})
private Integer type;
```

要点：每个字段 `@Description`；数值字段用包装类（`Long`/`Integer`/`Double`/`Boolean`）；外键 `@ManyToOne(fetch = EAGER)`；枚举实现
`IDictionary` 并用 `Integer` 存储。

### 8.6 方法 / 异常 / 注入

- 方法必须写 JavaDoc（用途 + 参数）。
- 空值判断用 `Objects.isNull()` / `Objects.nonNull()`，参数加 `@NotNull`。
- `switch` 表达式用 `->` 语法，`default` 留空。
- 业务异常用 `Errors.FORBIDDEN_xxx.when(条件, "提示")`， **不要抛 `RuntimeException`**：

  ```java
  //  Errors.FORBIDDEN_EDIT.when(subtract < 0, "库存数量不足");
  ```

- 依赖注入：`@Autowired` 直接打在字段上（如 `private StorageService storageService;`），不强制构造器注入。

### 8.7 代码格式化

- 4 空格缩进，不用 Tab。
- 大括号不换行，跟随语句。
- 每个方法之间保留一个空行。

## 9. 导入包顺序

```
1. java.*
2. jakarta.*
3. org.*
4. springframework.*
5. cn.hamm.airpower.*     ← 唯一允许通配符导入的包（AGENT.md 第 281 行明确"不使用通配符"，第 218 行 "import cn.hamm.airpower.*" 是例外）
6. cn.hamm.spms.*
7. lombok.*
```

其余包按类单独导入，不要 `java.util.*` 这种通配。

## 10. 数据库规范

- 表名小写 + 下划线分隔。
- 所有表继承基类的 `id`、`createTime`、`updateTime`。
- 整数用 `unsigned`；字符串必须指定长度；小数统一 `double(20, 6)`；布尔用 `bit(1) default 0`。
- 每个字段加 `comment '...'` 说明用途（写在 `columnDefinition` 内）。

## 11. AirPower4J 框架用法

| 场景           | API                                                                                          |
|----------------|----------------------------------------------------------------------------------------------|
| CURD 接口暴露  | `@Extends({GetDetail, GetPage, Add, Update, Delete})`                                        |
| 自定义查询条件 | Service 重写 `beforeGetPage` / `beforeCreatePredicate` / `addSearchPredicate`                |
| 字典取值       | `DictionaryUtil.getDictionary(EnumClass, value)`                                             |
| 树形子节点     | `TreeUtil.getChildrenIdList(id, supplier)`                                                   |
| 精确数字运算   | `NumberUtil.add(a, b)` / `NumberUtil.subtract(a, b)`（**不要直接 `+/- double`**）            |
| 并发安全更新   | `service.updateWithLock(id, consumer)`                                                       |
| MCP 扫描       | `McpService.scanMcpMethods("cn.hamm.spms", "cn.hamm.airpower")`（已在 `SpmsDevData` 中调用） |

## 12. 依赖新增原则

- 优先使用 AirPower4J 已提供的能力（绝大多数 CURD / WebSocket / MCP / 字典 / 异常都能取）。
- 不随意新增第三方依赖，确实需要时先评审。
- 新增依赖必须指定版本。

## 13. 部署与 Docker 陷阱

- **`deploy.sh` 与 `pom.xml` 版本不一致**：`deploy.sh` 硬编码 `JAR="server-3.0.0.jar"`，而 `pom.xml` 当前版本是 `4.0.0`。
  **直接执行会找不到 jar**——线上发布前必须把 `deploy.sh` 的 `JAR` 同步成当前 pom 版本，或在打包流水线里做变量替换。
- **`deploy.sh` 工作目录**：`DIR=/home/server/`、`DOWNLOAD=/home/app.tgz`，假设部署目标已存在该目录。仅适用于已配置好的生产服务器。
- **Dockerfile 会复制 `settings.xml`**：使用阿里云镜像（`mirrorOf=*,!central`），显式放行 Maven Central，从而保证 `cn.hamm`
  工件走 central。本地构建时也建议加 `-s settings.xml`，否则部分依赖可能拉取失败。
- **运行镜像**：`amazoncorretto:17-alpine`，不是官方 `openjdk`，注意 alpine 下的 glibc 兼容性（项目目前没有 native 依赖，但若新增
  native 库需重新评估）。
- **Dockerfile 构建命令**：`mvn clean package -DskipTests -s settings.xml`。

## 14. 不要做的事

- 不要修改 `airpower` 父 POM 的版本或新增覆盖其管理的依赖版本。
- 不要把本地密钥提交到 `application-local-*.yml`（`.gitignore` 虽已忽略，但 `--force` 也可能被加回）。
- 不要直接修改 `application.yml` 的 `ddl-auto: validate` 来"修一下生产"——生产 profile 已被显式覆盖为 `update`。
- 不要新增第三方依赖而不评估 AirPower 是否已提供（见 §12）。
- 不要在没有 `init.lock` 删除的情况下重启服务来"重新生成种子数据"——优先调对应 service 的 API。
- 不要在 Controller 自己手写 CURD 方法——用 `@Extends`。
- 不要直接 `a + b` / `a - b` 算 `Double`——用 `NumberUtil.add/subtract`。
- 不要抛 `RuntimeException`——用 `Errors.FORBIDDEN_xxx.when(...)`。

## 15. 代码审查 Checklist

- [ ] 包路径、类命名符合 §6 / §8 规范
- [ ] 文件头有 `<h1>` 注释 + `@author Hamm.cn`
- [ ] 类继承了正确的基类（§7）
- [ ] 实体类注解顺序与 §8.2 完全一致
- [ ] Controller 用 `@Extends` 暴露接口，没有自写方法
- [ ] 每个字段有 `@Description`；`@Column.columnDefinition` 含类型 + 默认值 + comment
- [ ] 外键是 `@ManyToOne(fetch = EAGER)`
- [ ] 字典字段类型为 `Integer`，枚举实现 `IDictionary`
- [ ] 方法有 JavaDoc；参数用包装类 + `@NotNull`
- [ ] 异常用 `Errors.FORBIDDEN_xxx.when(...)`
- [ ] 数字运算用 `NumberUtil.add/subtract`
- [ ] 并发更新用 `service.updateWithLock(id, consumer)`
- [ ] 导入顺序符合 §9；除 `cn.hamm.airpower.*` 外不出现通配符
- [ ] 4 空格缩进，大括号同行，方法间空行

## 16. 关键参考

- 上游基础框架：<https://github.com/AirPowerTeam/AirPower4J>
- 一键 Docker 部署仓库：<https://github.com/s-pms/SPMS-Docker>
- 在线 Demo：<https://spms.hamm.cn>
- 模板代码：`src/main/java/cn/hamm/spms/module/wms/inventory/`、`src/main/java/cn/hamm/spms/module/factory/storage/`