package com.qqlab.spms;

import cn.hamm.airpower.security.PasswordUtil;
import cn.hamm.airpower.util.redis.RedisUtil;
import cn.hutool.core.util.RandomUtil;
import com.qqlab.spms.module.asset.device.DeviceEntity;
import com.qqlab.spms.module.asset.device.DeviceService;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.asset.material.MaterialService;
import com.qqlab.spms.module.asset.material.MaterialType;
import com.qqlab.spms.module.channel.customer.CustomerEntity;
import com.qqlab.spms.module.channel.customer.CustomerService;
import com.qqlab.spms.module.channel.supplier.SupplierEntity;
import com.qqlab.spms.module.channel.supplier.SupplierService;
import com.qqlab.spms.module.factory.storage.StorageEntity;
import com.qqlab.spms.module.factory.storage.StorageService;
import com.qqlab.spms.module.factory.structure.StructureEntity;
import com.qqlab.spms.module.factory.structure.StructureService;
import com.qqlab.spms.module.iot.parameter.ParameterEntity;
import com.qqlab.spms.module.iot.parameter.ParameterService;
import com.qqlab.spms.module.iot.report.ReportDataType;
import com.qqlab.spms.module.iot.report.ReportEvent;
import com.qqlab.spms.module.personnel.role.RoleEntity;
import com.qqlab.spms.module.personnel.role.RoleService;
import com.qqlab.spms.module.personnel.user.UserEntity;
import com.qqlab.spms.module.personnel.user.UserService;
import com.qqlab.spms.module.system.app.AppEntity;
import com.qqlab.spms.module.system.app.AppService;
import com.qqlab.spms.module.system.coderule.CodeRuleEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import com.qqlab.spms.module.system.coderule.CodeRuleService;
import com.qqlab.spms.module.system.coderule.SerialNumberUpdate;
import com.qqlab.spms.module.system.menu.MenuEntity;
import com.qqlab.spms.module.system.menu.MenuService;
import com.qqlab.spms.module.system.permission.PermissionService;
import com.qqlab.spms.module.system.unit.UnitEntity;
import com.qqlab.spms.module.system.unit.UnitService;
import com.qqlab.spms.module.wms.inventory.InventoryEntity;
import com.qqlab.spms.module.wms.inventory.InventoryService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <h1>初始化</h1>
 *
 * @author hamm
 */
@Component
public class Initializer {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private AppService appService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private CodeRuleService codeRuleService;
    @Autowired
    private MaterialService materialService;
    @Autowired
    private UnitService unitService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private StructureService structureService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ParameterService parameterService;
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ReportEvent reportEvent;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Autowired
    private RedisUtil redisUtil;

    public void run() throws MqttException {
        redisUtil.clearAll();
        if ("create-drop".equals(ddlAuto)) {
            initUserAndRole();
            initCodeRules();
            initUnitAndMaterial();
            initFactory();
            initMenu();
            initOtherData();
            initParameters();
            initDevices();
            permissionService.forceReloadAllPermissions();
        }
        reportEvent.listen();
    }

    private void initDevices() {
        for (int i = 0; i < 10; i++) {
            deviceService.add(new DeviceEntity().setCode("Simulator00" + (i + 1)).setName("设备" + (i + 1)));
        }
    }

    private void initParameters() {
        parameterService.add(new ParameterEntity()
                .setId(1L).setCode(ReportEvent.REPORT_KEY_OF_STATUS)
                .setLabel("运行状态")
                .setDataType(ReportDataType.STATUS.getValue())
                .setIsSystem(true)
        );
        parameterService.add(new ParameterEntity()
                .setId(1L).setCode(ReportEvent.REPORT_KEY_OF_ALARM)
                .setLabel("报警状态")
                .setDataType(ReportDataType.STATUS.getValue())
                .setIsSystem(true)
        );
        parameterService.add(new ParameterEntity()
                .setId(1L).setCode(ReportEvent.REPORT_KEY_OF_PART_COUNT)
                .setLabel("实时产量")
                .setDataType(ReportDataType.QUANTITY.getValue())
                .setIsSystem(true)
        );
    }

    private void initOtherData() {
        appService.add(new AppEntity().setAppKey("airpower").setAppName("第三方应用").setUrl("").setAppSecret("abcdefghijklmnopqrstuvwxyz"));

        supplierService.add(new SupplierEntity().setCode("SP01").setName("三星屏幕套件"));
        customerService.add(new CustomerEntity().setCode("CUS01").setName("重庆解放碑AppleStore"));


        inventoryService.add(new InventoryEntity()
                .setMaterial(materialService.getById(1L))
                .setQuantity(2D)
                .setStorage(storageService.getById(1L))
        );
        inventoryService.add(new InventoryEntity()
                .setMaterial(materialService.getById(1L))
                .setQuantity(10D)
                .setStorage(storageService.getById(2L))
        );

    }

    private void initFactory() {
        storageService.add(new StorageEntity().setCode("sw01").setName("西南一区").setParentId(storageService.add(new StorageEntity().setName("西南大仓").setCode("east")).getId()));
        structureService.add(new StructureEntity().setCode("room1").setName("车间1").setParentId(structureService.add(new StructureEntity().setName("产线1").setCode("line1")).getId()));
    }

    private void initCodeRules() {
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.RoleCode.getValue()).setPrefix(CodeRuleField.RoleCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getValue())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.SupplierCode.getValue()).setPrefix(CodeRuleField.SupplierCode.getDefaultPrefix()).setTemplate("yyyy").setSnType(SerialNumberUpdate.YEAR.getValue())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.StorageCode.getValue()).setPrefix(CodeRuleField.StorageCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getValue())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.StructureCode.getValue()).setPrefix(CodeRuleField.StructureCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getValue())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.CustomerCode.getValue()).setPrefix(CodeRuleField.CustomerCode.getDefaultPrefix()).setTemplate("yyyy").setSnType(SerialNumberUpdate.YEAR.getValue())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.MaterialCode.getValue()).setPrefix(CodeRuleField.MaterialCode.getDefaultPrefix()).setTemplate("yyyy").setSnType(SerialNumberUpdate.YEAR.getValue())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.UnitCode.getValue()).setPrefix(CodeRuleField.UnitCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getValue())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.PurchaseBillCode.getValue()).setPrefix(CodeRuleField.PurchaseBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.SaleBillCode.getValue()).setPrefix(CodeRuleField.SaleBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.PlanBillCode.getValue()).setPrefix(CodeRuleField.PlanBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.OrderBillCode.getValue()).setPrefix(CodeRuleField.OrderBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.PickoutBillCode.getValue()).setPrefix(CodeRuleField.OrderBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.RestoreBillCode.getValue()).setPrefix(CodeRuleField.RestoreBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.InputBillCode.getValue()).setPrefix(CodeRuleField.InputBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.OutputBillCode.getValue()).setPrefix(CodeRuleField.OutputBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.MoveBillCode.getValue()).setPrefix(CodeRuleField.MoveBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.DeviceCode.getValue()).setPrefix(CodeRuleField.DeviceCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.OperationCode.getValue()).setPrefix(CodeRuleField.OperationCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getValue()).setTemplate("yyyymmdd")
        );
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    private void initMenu() {
        MenuEntity exist = menuService.getByIdMaybeNull(1L);
        if (Objects.nonNull(exist)) {
            return;
        }
        MenuEntity homeMenu = new MenuEntity().setName("首页").setOrderNo(99).setPath("/console").setComponent("/console/index/index").setParentId(0L);
        menuService.add(homeMenu);

        // 基础数据
        MenuEntity commonMenu = new MenuEntity().setName("基础数据").setOrderNo(88).setParentId(0L);
        commonMenu = menuService.add(commonMenu);

        MenuEntity commonSubMenu;
        MenuEntity commonFunctionMenu;

        commonSubMenu = new MenuEntity().setName("人事管理").setParentId(commonMenu.getId());
        commonSubMenu = menuService.add(commonSubMenu);

        commonFunctionMenu = new MenuEntity().setName("员工管理").setPath("/console/personnel/user/list").setParentId(commonSubMenu.getId());
        menuService.add(commonFunctionMenu);

        commonFunctionMenu = new MenuEntity().setName("角色管理").setPath("/console/personnel/role/list").setParentId(commonSubMenu.getId());
        menuService.add(commonFunctionMenu);

        commonSubMenu = new MenuEntity().setName("资产管理").setParentId(commonMenu.getId());
        commonSubMenu = menuService.add(commonSubMenu);

        commonFunctionMenu = new MenuEntity().setName("物料管理").setPath("/console/asset/material/list").setParentId(commonSubMenu.getId());
        menuService.add(commonFunctionMenu);

        commonFunctionMenu = new MenuEntity().setName("设备管理").setPath("/console/asset/device/list").setParentId(commonSubMenu.getId());
        menuService.add(commonFunctionMenu);

        commonSubMenu = new MenuEntity().setName("工厂模型").setParentId(commonMenu.getId());
        commonSubMenu = menuService.add(commonSubMenu);

        commonFunctionMenu = new MenuEntity().setName("存储资源").setPath("/console/factory/storage/list").setParentId(commonSubMenu.getId());
        menuService.add(commonFunctionMenu);

        commonFunctionMenu = new MenuEntity().setName("工厂结构").setPath("/console/factory/structure/list").setParentId(commonSubMenu.getId());
        menuService.add(commonFunctionMenu);

        // 渠道管理
        MenuEntity sourceMenu = new MenuEntity().setName("渠道管理").setOrderNo(77).setParentId(0L);
        sourceMenu = menuService.add(sourceMenu);

        MenuEntity sourceSubMenu;

        sourceSubMenu = new MenuEntity().setName("采购管理").setPath("/console/channel/purchase/list").setParentId(sourceMenu.getId());
        menuService.add(sourceSubMenu);

        sourceSubMenu = new MenuEntity().setName("销售管理").setPath("/console/channel/sale/list").setParentId(sourceMenu.getId());
        menuService.add(sourceSubMenu);

        sourceSubMenu = new MenuEntity().setName("供应商管理").setPath("/console/channel/supplier/list").setParentId(sourceMenu.getId());
        menuService.add(sourceSubMenu);

        sourceSubMenu = new MenuEntity().setName("客户管理").setPath("/console/channel/customer/list").setParentId(sourceMenu.getId());
        menuService.add(sourceSubMenu);

        sourceSubMenu = new MenuEntity().setName("采购价管理").setPath("/console/channel/purchasePrice/list").setParentId(sourceMenu.getId());
        menuService.add(sourceSubMenu);

        sourceSubMenu = new MenuEntity().setName("销售价管理").setPath("/console/channel/salePrice/list").setParentId(sourceMenu.getId());
        menuService.add(sourceSubMenu);

        // 仓储管理 - WMS
        MenuEntity wmsMenu = new MenuEntity().setName("仓储管理").setOrderNo(66).setParentId(0L);
        wmsMenu = menuService.add(wmsMenu);

        MenuEntity wmsSubMenu;
        wmsSubMenu = new MenuEntity().setName("库存概览").setPath("/console/wms/inventory/list").setParentId(wmsMenu.getId());
        menuService.add(wmsSubMenu);

        wmsSubMenu = new MenuEntity().setName("入库管理").setPath("/console/wms/input/list").setParentId(wmsMenu.getId());
        menuService.add(wmsSubMenu);

        wmsSubMenu = new MenuEntity().setName("出库管理").setPath("/console/wms/output/list").setParentId(wmsMenu.getId());
        menuService.add(wmsSubMenu);

        wmsSubMenu = new MenuEntity().setName("移库管理").setPath("/console/wms/move/list").setParentId(wmsMenu.getId());
        menuService.add(wmsSubMenu);

        // 生产管理 - MES
        MenuEntity mesMenu = new MenuEntity().setName("生产管理").setOrderNo(55).setParentId(0L);
        mesMenu = menuService.add(mesMenu);

        MenuEntity mesSubMenu;
        MenuEntity mesFunctionMenu;
        mesSubMenu = new MenuEntity().setName("生产资源").setParentId(mesMenu.getId());
        mesSubMenu = menuService.add(mesSubMenu);

        mesFunctionMenu = new MenuEntity().setName("物料领取").setPath("/console/mes/pickout/list").setParentId(mesSubMenu.getId());
        menuService.add(mesFunctionMenu);
        mesFunctionMenu = new MenuEntity().setName("物料退还").setPath("/console/mes/restore/list").setParentId(mesSubMenu.getId());
        menuService.add(mesFunctionMenu);

        mesSubMenu = new MenuEntity().setName("工艺工序").setParentId(mesMenu.getId());
        mesSubMenu = menuService.add(mesSubMenu);

        mesFunctionMenu = new MenuEntity().setName("工艺流程").setPath("/console/mes/process/list").setParentId(mesSubMenu.getId());
        menuService.add(mesFunctionMenu);
        mesFunctionMenu = new MenuEntity().setName("工序管理").setPath("/console/mes/operation/list").setParentId(mesSubMenu.getId());
        menuService.add(mesFunctionMenu);
        mesFunctionMenu = new MenuEntity().setName("BOM管理").setPath("/console/mes/bom/list").setParentId(mesSubMenu.getId());
        menuService.add(mesFunctionMenu);

        mesSubMenu = new MenuEntity().setName("生产执行").setParentId(mesMenu.getId());
        mesSubMenu = menuService.add(mesSubMenu);

        mesFunctionMenu = new MenuEntity().setName("生产计划").setPath("/console/mes/plan/list").setParentId(mesSubMenu.getId());
        menuService.add(mesFunctionMenu);
        mesFunctionMenu = new MenuEntity().setName("生产订单").setPath("/console/mes/order/list").setParentId(mesSubMenu.getId());
        menuService.add(mesFunctionMenu);


        // 物联网
        MenuEntity iotMenu = new MenuEntity().setName("物联网").setOrderNo(44).setParentId(0L);
        iotMenu = menuService.add(iotMenu);

        MenuEntity iotSubMenu;
        iotSubMenu = new MenuEntity().setName("设备概览").setPath("/console/iot/monitor/preview").setParentId(iotMenu.getId());
        menuService.add(iotSubMenu);

        iotSubMenu = new MenuEntity().setName("参数管理").setPath("/console/iot/parameter/list").setParentId(iotMenu.getId());
        menuService.add(iotSubMenu);

        // 系统设置
        MenuEntity sysMenu = new MenuEntity().setName("系统设置").setOrderNo(2).setParentId(0L);
        sysMenu = menuService.add(sysMenu);

        MenuEntity sysSubMenu;

        sysSubMenu = new MenuEntity().setName("计量单位").setPath("/console/system/unit/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);

        sysSubMenu = new MenuEntity().setName("编码规则").setPath("/console/system/coderule/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);

        sysSubMenu = new MenuEntity().setName("权限管理").setPath("/console/system/permission/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);

        sysSubMenu = new MenuEntity().setName("菜单管理").setPath("/console/system/menu/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);

        sysSubMenu = new MenuEntity().setName("第三方应用").setPath("/console/system/app/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);
    }

    private void initUserAndRole() {
        // 初始化角色
        RoleEntity firstRole = roleService.getByIdMaybeNull(1L);
        if (Objects.nonNull(firstRole)) {
            return;
        }
        firstRole = roleService.add(new RoleEntity()
                .setName("超级管理员")
                .setCode("ROOT")
                .setIsSystem(true)
                .setRemark("超级管理员角色组,请勿数据库暴力直接删除"));

        // 初始化用户
        UserEntity userEntity = userService.getByIdMaybeNull(1L);
        if (Objects.nonNull(userEntity)) {
            return;
        }
        Set<RoleEntity> roleList = new HashSet<>();
        roleList.add(firstRole);
        String salt = RandomUtil.randomString(4);
        userService.add(new UserEntity()
                .setNickname("Hamm")
                .setAccount("hamm")
                .setPhone("18523749565")
                .setEmail("admin@hamm.cn")
                .setPassword(PasswordUtil.encode("Aa123456", salt))
                .setSalt(salt)
                .setIsSystem(true)
                .setId(1L)
                .setRoleList(roleList)
                .setRemark("超级管理员,请勿数据库暴力直接删除"));

        System.out.println("---------------------------------");
    }

    private void initUnitAndMaterial() {
        UnitEntity unitEntity = unitService.add(new UnitEntity().setCode("kg").setName("kg"));
        materialService.add(
                new MaterialEntity()
                        .setMaterialType(MaterialType.PRODUCT.getValue())
                        .setName("MacBookPro")
                        .setSpc("M1Pro-16G-512G-16Inch")
                        .setCode("macbook")
                        .setPurchasePrice(200D)
                        .setUnitInfo(unitEntity)
        );
    }
}
