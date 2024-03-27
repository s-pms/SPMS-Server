package cn.hamm.spms;

import cn.hamm.airpower.security.PasswordUtil;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.customer.CustomerService;
import cn.hamm.spms.module.channel.supplier.SupplierService;
import cn.hamm.spms.module.factory.storage.StorageService;
import cn.hamm.spms.module.factory.structure.StructureService;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.parameter.ParameterService;
import cn.hamm.spms.module.iot.report.ReportDataType;
import cn.hamm.spms.module.iot.report.ReportEvent;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.role.RoleService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.system.app.AppService;
import cn.hamm.spms.module.system.coderule.CodeRuleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import cn.hamm.spms.module.system.coderule.SerialNumberUpdate;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionService;
import cn.hamm.spms.module.system.unit.UnitService;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hutool.core.util.RandomUtil;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <h1>初始化</h1>
 *
 * @author Hamm
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

    @Autowired
    private AppConfig appConfig;

    public void run() throws MqttException {
        if (appConfig.isInitData()) {
            initUserAndRole();
            initCodeRules();
            initOtherData();
            initParameters();
            permissionService.initPermission();
            initMenu();
        }
        reportEvent.listen();
    }

    private void initParameters() {
        parameterService.add(new ParameterEntity()
                .setCode(ReportEvent.REPORT_KEY_OF_STATUS)
                .setLabel("运行状态")
                .setDataType(ReportDataType.STATUS.getKey())
                .setIsSystem(true)
        );
        parameterService.add(new ParameterEntity()
                .setCode(ReportEvent.REPORT_KEY_OF_ALARM)
                .setLabel("报警状态")
                .setDataType(ReportDataType.STATUS.getKey())
                .setIsSystem(true)
        );
        parameterService.add(new ParameterEntity()
                .setCode(ReportEvent.REPORT_KEY_OF_PART_COUNT)
                .setLabel("实时产量")
                .setDataType(ReportDataType.QUANTITY.getKey())
                .setIsSystem(true)
        );
    }

    private void initOtherData() {
    }

    private void initCodeRules() {
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.RoleCode.getKey()).setPrefix(CodeRuleField.RoleCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getKey())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.SupplierCode.getKey()).setPrefix(CodeRuleField.SupplierCode.getDefaultPrefix()).setTemplate("yyyy").setSnType(SerialNumberUpdate.YEAR.getKey())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.StorageCode.getKey()).setPrefix(CodeRuleField.StorageCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getKey())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.StructureCode.getKey()).setPrefix(CodeRuleField.StructureCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getKey())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.CustomerCode.getKey()).setPrefix(CodeRuleField.CustomerCode.getDefaultPrefix()).setTemplate("yyyy").setSnType(SerialNumberUpdate.YEAR.getKey())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.MaterialCode.getKey()).setPrefix(CodeRuleField.MaterialCode.getDefaultPrefix()).setTemplate("yyyy").setSnType(SerialNumberUpdate.YEAR.getKey())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.UnitCode.getKey()).setPrefix(CodeRuleField.UnitCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.NEVER.getKey())
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.PurchaseBillCode.getKey()).setPrefix(CodeRuleField.PurchaseBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.SaleBillCode.getKey()).setPrefix(CodeRuleField.SaleBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.PlanBillCode.getKey()).setPrefix(CodeRuleField.PlanBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.OrderBillCode.getKey()).setPrefix(CodeRuleField.OrderBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.PickoutBillCode.getKey()).setPrefix(CodeRuleField.OrderBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.RestoreBillCode.getKey()).setPrefix(CodeRuleField.RestoreBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.InputBillCode.getKey()).setPrefix(CodeRuleField.InputBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.OutputBillCode.getKey()).setPrefix(CodeRuleField.OutputBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.MoveBillCode.getKey()).setPrefix(CodeRuleField.MoveBillCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.DeviceCode.getKey()).setPrefix(CodeRuleField.DeviceCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
        codeRuleService.add(
                new CodeRuleEntity().setRuleField(CodeRuleField.OperationCode.getKey()).setPrefix(CodeRuleField.OperationCode.getDefaultPrefix()).setSnType(SerialNumberUpdate.DAY.getKey()).setTemplate("yyyymmdd")
        );
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    private void initMenu() {

        MenuEntity firstMenu;
        MenuEntity secondMenu;
        MenuEntity thirdMenu;

        firstMenu = new MenuEntity().setName("首页").setOrderNo(99).setPath("/console").setComponent("/console/index/index").setParentId(0L);
        menuService.add(firstMenu);

        // 基础数据
        firstMenu = new MenuEntity().setName("基础数据").setOrderNo(88).setParentId(0L);
        firstMenu = menuService.get(menuService.add(firstMenu));

        secondMenu = new MenuEntity().setName("人事管理").setParentId(firstMenu.getId());
        secondMenu = menuService.get(menuService.add(secondMenu));

        thirdMenu = new MenuEntity().setName("员工管理").setPath("/console/personnel/user/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);
        thirdMenu = new MenuEntity().setName("角色管理").setPath("/console/personnel/role/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);

        secondMenu = new MenuEntity().setName("资产管理").setParentId(firstMenu.getId());
        secondMenu = menuService.get(menuService.add(secondMenu));

        thirdMenu = new MenuEntity().setName("物料管理").setPath("/console/asset/material/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);
        thirdMenu = new MenuEntity().setName("设备管理").setPath("/console/asset/device/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);

        secondMenu = new MenuEntity().setName("工厂模型").setParentId(firstMenu.getId());
        secondMenu = menuService.get(menuService.add(secondMenu));

        thirdMenu = new MenuEntity().setName("存储资源").setPath("/console/factory/storage/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);
        thirdMenu = new MenuEntity().setName("工厂结构").setPath("/console/factory/structure/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);


        // 渠道管理
        firstMenu = new MenuEntity().setName("渠道管理").setOrderNo(77).setParentId(0L);
        firstMenu = menuService.get(menuService.add(firstMenu));

        secondMenu = new MenuEntity().setName("采购管理").setPath("/console/channel/purchase/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("销售管理").setPath("/console/channel/sale/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("供应商管理").setPath("/console/channel/supplier/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("客户管理").setPath("/console/channel/customer/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("采购价管理").setPath("/console/channel/purchasePrice/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("销售价管理").setPath("/console/channel/salePrice/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);


        // 仓储管理 - WMS
        firstMenu = new MenuEntity().setName("仓储管理").setOrderNo(66).setParentId(0L);
        firstMenu = menuService.get(menuService.add(firstMenu));

        secondMenu = new MenuEntity().setName("库存概览").setPath("/console/wms/inventory/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("入库管理").setPath("/console/wms/input/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("出库管理").setPath("/console/wms/output/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("移库管理").setPath("/console/wms/move/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);


        // 生产管理 - MES
        firstMenu = new MenuEntity().setName("生产管理").setOrderNo(55).setParentId(0L);
        firstMenu = menuService.get(menuService.add(firstMenu));

        secondMenu = new MenuEntity().setName("生产资源").setParentId(firstMenu.getId());
        secondMenu = menuService.get(menuService.add(secondMenu));

        thirdMenu = new MenuEntity().setName("物料领取").setPath("/console/mes/pickout/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);
        thirdMenu = new MenuEntity().setName("物料退还").setPath("/console/mes/restore/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);

        secondMenu = new MenuEntity().setName("工艺工序").setParentId(firstMenu.getId());
        secondMenu = menuService.get(menuService.add(secondMenu));

        thirdMenu = new MenuEntity().setName("工艺流程").setPath("/console/mes/process/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);
        thirdMenu = new MenuEntity().setName("工序管理").setPath("/console/mes/operation/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);
        thirdMenu = new MenuEntity().setName("BOM管理").setPath("/console/mes/bom/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);

        secondMenu = new MenuEntity().setName("生产执行").setParentId(firstMenu.getId());
        secondMenu = menuService.get(menuService.add(secondMenu));

        thirdMenu = new MenuEntity().setName("生产计划").setPath("/console/mes/plan/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);
        thirdMenu = new MenuEntity().setName("生产订单").setPath("/console/mes/order/list").setParentId(secondMenu.getId());
        menuService.add(thirdMenu);


        // 物联网
        firstMenu = new MenuEntity().setName("物联网").setOrderNo(44).setParentId(0L);
        firstMenu = menuService.get(menuService.add(firstMenu));

        MenuEntity iotSubMenu;
        iotSubMenu = new MenuEntity().setName("设备概览").setPath("/console/iot/monitor/preview").setParentId(firstMenu.getId());
        menuService.add(iotSubMenu);
        iotSubMenu = new MenuEntity().setName("参数管理").setPath("/console/iot/parameter/list").setParentId(firstMenu.getId());
        menuService.add(iotSubMenu);

        // 系统设置
        firstMenu = new MenuEntity().setName("系统设置").setOrderNo(2).setParentId(0L);
        firstMenu = menuService.get(menuService.add(firstMenu));

        secondMenu = new MenuEntity().setName("计量单位").setPath("/console/system/unit/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("编码规则").setPath("/console/system/coderule/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("权限管理").setPath("/console/system/permission/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("菜单管理").setPath("/console/system/menu/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
        secondMenu = new MenuEntity().setName("第三方应用").setPath("/console/system/app/list").setParentId(firstMenu.getId());
        menuService.add(secondMenu);
    }

    private void initUserAndRole() {
        // 初始化角色
        RoleEntity firstRole = roleService.getMaybeNull(1L);
        if (Objects.nonNull(firstRole)) {
            return;
        }
        long roleId = roleService.add(new RoleEntity()
                .setName("超级管理员")
                .setCode("ROOT")
                .setIsSystem(true)
                .setRemark("超级管理员角色组,请勿数据库暴力直接删除"));
        firstRole = roleService.get(roleId);

        // 初始化用户
        UserEntity userEntity = userService.getMaybeNull(1L);
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
}
