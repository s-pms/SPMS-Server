# SPMS-Server 开发规范

本文档约定了 **SPMS-Server** 项目的开发规范，供 AI 辅助开发（如 Qwen Code、Cursor、GitHub Copilot 等）参考，保证生成的代码与项目现有风格一致。

## 技术栈

| 技术              | 版本              | 说明     |
|-----------------|-----------------|--------|
| Java            | 17              | 开发语言   |
| Spring Boot     | 3.x             | 应用框架   |
| Maven           | 3.9+            | 依赖管理   |
| JPA + Hibernate | Spring Data JPA | ORM框架  |
| MySQL           | 8.x             | 数据库    |
| Redis           | -               | 缓存     |
| AirPower4J      | 6.x             | 核心基础框架 |
| Lombok          | -               | 代码简化   |

## 项目整体结构

```
SPMS-Server/
├── src/main/java/cn/hamm/spms/
│   ├── base/               # 项目基础类继承
│   ├── common/             # 公共工具类和常量
│   ├── module/             # 业务模块（按模块划分）
│   │   ├── asset/          # 资产模块
│   │   ├── channel/        # 渠道模块
│   │   ├── chat/           # 聊天模块
│   │   ├── factory/        # 工厂模块
│   │   ├── iot/            # 物联网模块
│   │   ├── mcp/            # MCP模块
│   │   ├── mes/            # MES模块
│   │   ├── open/           # 开放接口模块
│   │   ├── personnel/      # 人事模块
│   │   ├── system/         # 系统模块
│   │   ├── wechat/         # 微信模块
│   │   └── wms/            # WMS仓库管理模块
│   ├── Application.java    # 启动类
│   ├── DevDataInitRunner.java # 开发数据初始化
│   └── WebConfig.java      # Web配置
└── src/main/resources/
    ├── application.yml         # 主配置文件
    ├── application-template.yml # 配置模板
    ├── application-production.yml # 生产配置
    └── logback-spring.xml     # 日志配置
```

## 模块内部结构规范

每个业务模块下，按照业务功能划分分包，每个业务功能包包含以下文件：

```
module/
└── function/               # 业务功能包
    ├── enums/              # 枚举类目录（可选）
    │   └── XXXType.java    # 枚举定义
    ├── XXXEntity.java      # 实体类
    ├── XXXRepository.java  # 数据访问层接口
    ├── XXXService.java     # 业务逻辑层
    └── XXXController.java  # 接口控制层
```

### 命名规范

| 层次         | 命名规范                  | 示例                            |
|------------|-----------------------|-------------------------------|
| 包名         | 全小写，使用英文单词            | `inventory`、`input`、`output`  |
| 实体类        | 大驼峰命名 + Entity后缀      | `InventoryEntity`             |
| Repository | 大驼峰命名 + Repository后缀  | `InventoryRepository`         |
| Service    | 大驼峰命名 + Service后缀     | `InventoryService`            |
| Controller | 大驼峰命名 + Controller后缀  | `InventoryController`         |
| 枚举类        | 大驼峰命名 + Type/Status后缀 | `InventoryType`               |
| 方法名        | 小驼峰命名，见名知意            | `getByMaterialIdAndStorageId` |
| 变量名        | 小驼峰命名                 | `materialId`、`storageId`      |
| 常量         | 全大写下划线分隔              | `DEFAULT_PAGE_SIZE`           |

## 编码风格规范

### 1. 文件头注释

每个文件必须包含类说明和作者：

```java
/**
 * <h1>类描述</h1>
 *
 * @author Hamm.cn
 */
```

### 2. 类注解顺序（实体类示例）

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
    // ...
}
```

### 3. Controller 注解顺序

```java

@Api("api_path")
@Description("功能描述")
@Extends({GetDetail, GetPage})
public class XxxController extends BaseController<XxxEntity, XxxService, XxxRepository> {
}
```

一般情况下使用 `@Extends` 继承 CURD 接口即可，不需要重复写方法。常用的 CURD 接口：

- `GetDetail` - 根据ID查询详情
- `GetPage` - 分页查询
- `Add` - 新增
- `Update` - 更新
- `Delete` - 删除

### 4. Service 注解

```java

@Service
public class XxxService extends BaseService<XxxEntity, XxxRepository> {
    // ...
}
```

### 5. Repository 注解

```java

@Repository
public interface XxxRepository extends BaseRepository<XxxEntity> {
    // 自定义查询方法
}
```

### 6. 字段定义

- 使用 `@Description` 注解描述每个字段
- 数据库字段使用 `@Column` 定义 `columnDefinition`，包含类型和默认值注释
- 外键使用 `@ManyToOne`，指定 `fetch = EAGER`

示例：

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

### 7. 枚举使用

- 使用 `@Dictionary` 注解声明字典
- 枚举实现 `IDictionary` 接口
- 字段类型使用 `Integer` 存储枚举值

示例：

```java

@Description("存储类型")
@Column(columnDefinition = "int UNSIGNED default 1 comment '存储类型'")
@Dictionary(value = InventoryType.class, groups = {WhenAdd.class, WhenUpdate.class})
private Integer type;
```

### 8. 方法编写规范

- 方法必须添加 JavaDoc 注释，说明方法用途和参数
- 参数类型使用包装类（`Long`、`Integer`、`Double`、`Boolean`）而非基本类型
- 空值判断使用 `Objects.isNull()` 或 `Objects.nonNull()`
- 使用 `@NotNull` 注解标记不可为空的参数
- 条件分支使用 `switch` 表达式时，使用 `->` 语法，`default` 留空即可

### 9. 异常处理

使用 AirPower 的 `Errors` 抛出业务异常：

```java
Errors.FORBIDDEN_EDIT.when(subtract< 0, "库存数量不足");
```

### 10. 依赖注入

使用 `@Autowired` 注解注入依赖，放在字段上：

```java

@Autowired
private StorageService storageService;
```

### 11. 代码格式化

- 使用 4 空格缩进（不使用 Tab）
- 大括号不换行，跟随语句
- 每个方法之间保留一个空行
- 导入包使用 * 方式，如 `import cn.hamm.airpower.*;`

## 继承体系

所有类都继承项目提供的基类：

| 层次         | 基类                                                                                                  |
|------------|-----------------------------------------------------------------------------------------------------|
| Entity     | `BaseEntity<E extends BaseEntity<E>>`                                                               |
| Repository | `BaseRepository<E extends BaseEntity<E>>`                                                           |
| Service    | `BaseService<E extends BaseEntity<E>, R extends BaseRepository<E>>`                                 |
| Controller | `BaseController<E extends BaseEntity<E>, S extends BaseService<E, R>, R extends BaseRepository<E>>` |

## 数据库规范

- 表名使用下划线分隔小写命名
- 所有表继承基类的 `id`、`createTime`、`updateTime` 字段
- 使用 `unsigned` 非负整数
- 字符串字段必须指定长度
- 小数使用 `double(20, 6)` 格式
- 添加 `comment` 描述字段用途
- 使用 `bit(1)` 存储布尔类型，默认值 `0`

## AirPower4J 框架使用规范

### 1. CURD 扩展

Controller 使用 `@Extends` 注解继承需要暴露的 CURD 接口：

```java
@Extends({GetDetail, GetPage, Add, Update, Delete})
```

### 2. 查询扩展

Service 中重写 `beforeGetPage`、`beforeCreatePredicate`、`addSearchPredicate` 方法添加自定义查询条件。

### 3. 字典转换

使用 `DictionaryUtil.getDictionary(EnumClass, value)` 获取枚举实例。

### 4. 树形结构

使用 `TreeUtil.getChildrenIdList(id, supplier)` 获取所有子节点ID列表。

### 5. 数字计算

使用 `NumberUtil.add()`、`NumberUtil.subtract()` 进行精确计算，避免精度丢失。

### 6. 更新锁

使用 `updateWithLock(id, consumer)` 进行并发安全的更新。

## 导入包顺序

1. `java.*`
2. `jakarta.*`
3. `org.*`
4. `springframework.*`
5. `cn.hamm.airpower.*`
6. `cn.hamm.spms.*`
7. `lombok.*`

**导入包不使用通配符，每个类单独导入。**

## 代码审查 Checklist

开发完成后检查：

- [ ] 包路径是否正确
- [ ] 类命名是否符合规范
- [ ] 类注释和方法注释是否完整
- [ ] 是否继承了正确的基类
- [ ] 字段是否添加了 `@Description` 注解
- [ ] `@Column` 的 `columnDefinition` 是否正确定义
- [ ] 外键是否使用 `@ManyToOne(fetch = EAGER)`
- [ ] 作者是否为 `Hamm.cn`
- [ ] 代码格式是否正确
- [ ] 是否存在不必要的空行或空格

## 依赖新增原则

- 优先使用 `AirPower4J` 已提供的能力
- 不随意新增第三方依赖，确实需要时先评审
- 新增依赖必须指定版本

## 示例代码参考

完整的示例可以参考：

- `cn.hamm.spms.module.wms.inventory` 包下的代码
- `cn.hamm.spms.module.factory.storage` 包下的代码
