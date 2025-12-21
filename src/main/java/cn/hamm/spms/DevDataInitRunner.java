package cn.hamm.spms;

import cn.hamm.airpower.access.PasswordUtil;
import cn.hamm.airpower.mcp.McpService;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.asset.material.enums.MaterialType;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import cn.hamm.spms.module.channel.customer.CustomerService;
import cn.hamm.spms.module.channel.purchaseprice.PurchasePriceEntity;
import cn.hamm.spms.module.channel.purchaseprice.PurchasePriceService;
import cn.hamm.spms.module.channel.saleprice.SalePriceEntity;
import cn.hamm.spms.module.channel.saleprice.SalePriceService;
import cn.hamm.spms.module.channel.supplier.SupplierEntity;
import cn.hamm.spms.module.channel.supplier.SupplierService;
import cn.hamm.spms.module.chat.room.RoomEntity;
import cn.hamm.spms.module.chat.room.RoomService;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.factory.storage.StorageService;
import cn.hamm.spms.module.factory.structure.StructureEntity;
import cn.hamm.spms.module.factory.structure.StructureService;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.parameter.ParameterService;
import cn.hamm.spms.module.iot.report.enums.ReportDataType;
import cn.hamm.spms.module.mes.bom.BomEntity;
import cn.hamm.spms.module.mes.bom.BomService;
import cn.hamm.spms.module.mes.bom.detail.BomDetailEntity;
import cn.hamm.spms.module.mes.bom.enums.BomType;
import cn.hamm.spms.module.mes.operation.OperationEntity;
import cn.hamm.spms.module.mes.operation.OperationService;
import cn.hamm.spms.module.mes.routing.RoutingEntity;
import cn.hamm.spms.module.mes.routing.RoutingService;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationEntity;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
import cn.hamm.spms.module.personnel.department.DepartmentService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.personnel.user.department.UserDepartmentEntity;
import cn.hamm.spms.module.personnel.user.department.UserDepartmentService;
import cn.hamm.spms.module.system.coderule.CodeRuleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleField;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigService;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionService;
import cn.hamm.spms.module.system.unit.UnitEntity;
import cn.hamm.spms.module.system.unit.UnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import static cn.hamm.spms.module.iot.report.ReportConstant.*;

/**
 * <h1>初始化</h1>
 *
 * @author Hamm.cn
 */
@Component
@Slf4j
public class DevDataInitRunner implements CommandLineRunner {
    public static final int TWO = 2;
    @Autowired
    private ParameterService parameterService;
    @Autowired
    private CodeRuleService codeRuleService;
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private UnitService unitService;
    @Autowired
    private MaterialService materialService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private SalePriceService salePriceService;
    @Autowired
    private PurchasePriceService purchasePriceService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private OperationService operationService;
    @Autowired
    private BomService bomService;
    @Autowired
    private RoutingService routingService;
    @Autowired
    private StructureService structureService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private UserDepartmentService userDepartmentService;

    @Autowired
    private AppConfig appConfig;

    @Override
    public void run(String... args) {
        McpService.scanMcpMethods("cn.hamm.spms", "cn.hamm.airpower");
        if (!appConfig.getIsDevMode()) {
            log.info("非开发者模式，无需初始化数据");
            return;
        }
        // 判断运行目录是否存在 init.lock 文件，如存在，则不初始化数据
        if (new File("init.lock").exists()) {
            log.info("已存在 init.lock 文件，无需初始化数据");
            return;
        }
        permissionService.initMcpToolPermission(McpService.tools);
        initRootUser();
        initCodeRules();
        initConfigs();
        initParameters();
        permissionService.loadPermission();
        menuService.initMenu();
        initDevData();
        try {
            //noinspection ResultOfMethodCallIgnored
            new File("init.lock").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initParameters() {
        ParameterEntity parameter;

        parameter = parameterService.getByCode(REPORT_KEY_OF_STATUS);
        if (Objects.isNull(parameter)) {
            parameterService.addAndGet(new ParameterEntity()
                    .setCode(REPORT_KEY_OF_STATUS)
                    .setLabel("运行状态")
                    .setDataType(ReportDataType.STATUS.getKey())
                    .setIsSystem(true)
            );
        }
        parameter = parameterService.getByCode(REPORT_KEY_OF_ALARM);
        if (Objects.isNull(parameter)) {
            parameterService.addAndGet(new ParameterEntity()
                    .setCode(REPORT_KEY_OF_ALARM)
                    .setLabel("报警状态")
                    .setDataType(ReportDataType.STATUS.getKey())
                    .setIsSystem(true)
            );
        }
        parameter = parameterService.getByCode(REPORT_KEY_OF_PART_COUNT);
        if (Objects.isNull(parameter)) {
            parameterService.addAndGet(new ParameterEntity()
                    .setCode(REPORT_KEY_OF_PART_COUNT)
                    .setLabel("实时产量")
                    .setDataType(ReportDataType.NUMBER.getKey())
                    .setIsSystem(true)
            );
        }
    }

    private void initCodeRules() {
        CodeRuleField[] codeRuleFields = CodeRuleField.class.getEnumConstants();
        for (CodeRuleField codeRuleField : codeRuleFields) {
            CodeRuleEntity codeRule = codeRuleService.getByRuleField(codeRuleField.getKey());
            if (Objects.isNull(codeRule)) {
                codeRuleService.addAndGet(
                        new CodeRuleEntity()
                                .setIsSystem(true)
                                .setRuleField(codeRuleField.getKey())
                                .setPrefix(codeRuleField.getDefaultPrefix())
                                .setTemplate(codeRuleField.getDefaultSnType().getDefaultTemplate())
                                .setSnType(codeRuleField.getDefaultSnType().getKey())
                );
            }
        }
    }

    private void initRootUser() {
        // 初始化用户
        UserEntity user = userService.getMaybeNull(1L);
        if (Objects.nonNull(user)) {
            return;
        }
        DepartmentEntity department = departmentService.addAndGet(new DepartmentEntity().setCode("a1").setName("生产部"));
        departmentService.addAndGet(new DepartmentEntity().setName("材料部").setCode("aaa1").setParentId(department.getId()));
        String salt = RandomUtil.randomString(UserService.PASSWORD_SALT_LENGTH);
        user = userService.addAndGet(new UserEntity()
                .setNickname("凌小云")
                .setPhone("17666666666")
                .setEmail("admin@hamm.cn")
                .setPassword(PasswordUtil.encode("Aa123456", salt))
                .setSalt(salt)
        );
        userService.addAndGet(new UserEntity()
                .setNickname("张三")
                .setPhone("13888888888")
                .setEmail("admin@hamm.com")
                .setPassword(PasswordUtil.encode("Aa123456", salt))
                .setSalt(salt));
        userDepartmentService.addAndGet(new UserDepartmentEntity()
                .setUser(user)
                .setDepartment(department)
        );
        user = userService.getMaybeNull(1L);

        roomService.addAndGet(new RoomEntity()
                .setName("广场")
                .setCode(666)
                .setIsOfficial(true)
                .setIsHot(true).setOwner(user)
        );

        roomService.addAndGet(new RoomEntity()
                .setName("测试")
                .setCode(888)
                .setIsHot(true).setOwner(user)
        );
    }

    private void initConfigs() {
        ConfigFlag[] configFlags = ConfigFlag.class.getEnumConstants();
        for (ConfigFlag configFlag : configFlags) {
            try {
                ConfigEntity config = configService.get(configFlag);
                if (Objects.nonNull(config)) {
                    continue;
                }
            } catch (RuntimeException exception) {
                log.info("需要初始化配置");
            }
            configService.addAndGet(new ConfigEntity()
                    .setConfig(configFlag.getDefaultValue())
                    .setType(configFlag.getType().getKey())
                    .setName(configFlag.getLabel())
                    .setFlag(configFlag.name())
                    .setDescription(configFlag.getDescription())
            );
        }
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    private void initDevData() {
        int deviceCount = 2;
        for (int i = 0; i < deviceCount; i++) {
            deviceService.addAndGet(new DeviceEntity().setCode("Simulator00" + (i + 1)).setName("设备" + (i + 1)));
        }

        UnitEntity unit = new UnitEntity();
        unit.setName("台");
        unit = unitService.addAndGet(unit);

        MaterialEntity material = new MaterialEntity()
                .setMaterialType(MaterialType.PURCHASE.getKey())
                .setName("MacBook Pro M4 Max")
                .setSpc("32G-1TB")
                .setUnit(unit)
                .setPurchasePrice(28000D)
                .setSalePrice(29999D);
        material = materialService.addAndGet(material);

        CustomerEntity customer = new CustomerEntity();
        customer.setName("腾讯科技").setPhone("17666666666");
        customer = customerService.addAndGet(customer);

        SupplierEntity supplier = new SupplierEntity();
        supplier.setName("Apple中国").setPhone("17666666666");
        supplier = supplierService.addAndGet(supplier);

        SalePriceEntity salePrice = new SalePriceEntity();
        salePrice.setCustomer(customer).setPrice(29999D).setMaterial(material);
        salePriceService.addAndGet(salePrice);

        PurchasePriceEntity purchasePrice = new PurchasePriceEntity();
        purchasePrice.setSupplier(supplier).setPrice(28000D).setMaterial(material);
        purchasePriceService.addAndGet(purchasePrice);

        StorageEntity storage = new StorageEntity().setName("东部大仓");
        storageService.addAndGet(storage);

        for (int i = 0; i < TWO; i++) {
            storageService.addAndGet(new StorageEntity()
                    .setParentId(storage.getId())
                    .setName(String.format("东部%s仓", (i + 1)))
            );
        }

        storage = new StorageEntity().setName("西部大仓");
        storage = storageService.addAndGet(storage);

        for (int i = 0; i < TWO; i++) {
            storageService.addAndGet(new StorageEntity()
                    .setParentId(storage.getId())
                    .setName(String.format("西部%s仓", (i + 1)))
            );
        }

        OperationEntity operationKeyboard = operationService.addAndGet(new OperationEntity().setName("键盘安装"));
        OperationEntity operationScreen = operationService.addAndGet(new OperationEntity().setName("屏幕贴膜"));
        OperationEntity operationSystem = operationService.addAndGet(new OperationEntity().setName("系统安装"));

        MaterialEntity materialKeyboard = materialService.addAndGet(new MaterialEntity().setName("键盘").setMaterialType(MaterialType.PURCHASE.getKey()).setUnit(unit));
        MaterialEntity materialScreen = materialService.addAndGet(new MaterialEntity().setName("屏幕").setMaterialType(MaterialType.PURCHASE.getKey()).setUnit(unit));

        BomEntity bomComputer = bomService.addAndGet(new BomEntity().setName("笔记本电脑清单").setType(BomType.NORMAL.getKey()).setDetails(
                new HashSet<>(Arrays.asList(
                        new BomDetailEntity().setMaterial(materialKeyboard).setQuantity(1D),
                        new BomDetailEntity().setMaterial(materialScreen).setQuantity(1D)
                ))
        ));

        BomEntity bomKeyboard = bomService.addAndGet(new BomEntity().setName("键盘安装清单").setType(BomType.OPERATION.getKey()).setDetails(
                new HashSet<>(Collections.singletonList(
                        new BomDetailEntity().setMaterial(materialKeyboard).setQuantity(1D)
                ))
        ));
        BomEntity bomIdScreen = bomService.addAndGet(new BomEntity().setName("屏幕贴膜清单").setType(BomType.OPERATION.getKey()).setDetails(
                new HashSet<>(Collections.singletonList(
                        new BomDetailEntity().setMaterial(materialScreen).setQuantity(1D)
                ))
        ));

        routingService.addAndGet(new RoutingEntity().setBom(bomComputer)
                .setName("笔记本安装")
                .setMaterial(material)
                .setDetails(Arrays.asList(
                        new RoutingOperationEntity().setBom(bomKeyboard)
                                .setIsAutoNext(true)
                                .setOperation(operationKeyboard),
                        new RoutingOperationEntity().setBom(bomIdScreen)
                                .setIsAutoNext(true)
                                .setOperation(operationScreen),
                        new RoutingOperationEntity().setOperation(operationSystem)
                ))
        );

        StructureEntity structure = new StructureEntity().setName("笔记本电脑产线");
        structureService.addAndGet(structure);

        for (int i = 0; i < TWO; i++) {
            structureService.addAndGet(new StructureEntity()
                    .setParentId(structure.getId())
                    .setName(String.format("工位%s", (i + 1)))
                    .setOperationList(new HashSet<>(Arrays.asList(
                            operationKeyboard,
                            operationScreen,
                            operationSystem
                    )))
            );
        }

    }
}
