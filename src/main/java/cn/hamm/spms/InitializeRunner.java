package cn.hamm.spms;

import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.config.AppConstant;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.asset.material.MaterialType;
import cn.hamm.spms.module.channel.customer.CustomerEntity;
import cn.hamm.spms.module.channel.purchaseprice.PurchasePriceEntity;
import cn.hamm.spms.module.channel.saleprice.SalePriceEntity;
import cn.hamm.spms.module.channel.supplier.SupplierEntity;
import cn.hamm.spms.module.factory.storage.StorageEntity;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.report.ReportDataType;
import cn.hamm.spms.module.iot.report.ReportEvent;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import cn.hamm.spms.module.system.unit.UnitEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

/**
 * <h1>初始化</h1>
 *
 * @author Hamm.cn
 */
@Component
public class InitializeRunner implements CommandLineRunner {

    public static final int FOUR = 4;

    private void initParameters() {
        ParameterEntity parameter;

        parameter = Services.getParameterService().getByCode(ReportEvent.REPORT_KEY_OF_STATUS);
        if (Objects.isNull(parameter)) {
            Services.getParameterService().add(new ParameterEntity()
                    .setCode(ReportEvent.REPORT_KEY_OF_STATUS)
                    .setLabel("运行状态")
                    .setDataType(ReportDataType.STATUS.getKey())
                    .setIsSystem(true)
            );
        }
        parameter = Services.getParameterService().getByCode(ReportEvent.REPORT_KEY_OF_ALARM);
        if (Objects.isNull(parameter)) {
            Services.getParameterService().add(new ParameterEntity()
                    .setCode(ReportEvent.REPORT_KEY_OF_ALARM)
                    .setLabel("报警状态")
                    .setDataType(ReportDataType.STATUS.getKey())
                    .setIsSystem(true)
            );
        }
        parameter = Services.getParameterService().getByCode(ReportEvent.REPORT_KEY_OF_PART_COUNT);
        if (Objects.isNull(parameter)) {
            Services.getParameterService().add(new ParameterEntity()
                    .setCode(ReportEvent.REPORT_KEY_OF_PART_COUNT)
                    .setLabel("实时产量")
                    .setDataType(ReportDataType.QUANTITY.getKey())
                    .setIsSystem(true)
            );
        }
    }

    private void initCodeRules() {
        CodeRuleField[] codeRuleFields = CodeRuleField.class.getEnumConstants();
        for (CodeRuleField codeRuleField : codeRuleFields) {
            CodeRuleEntity codeRule = Services.getCodeRuleService().getByRuleField(codeRuleField.getKey());
            if (Objects.isNull(codeRule)) {
                Services.getCodeRuleService().add(
                        new CodeRuleEntity()
                                .setIsSystem(true)
                                .setRuleField(codeRuleField.getKey())
                                .setPrefix(codeRuleField.getDefaultPrefix())
                                .setTemplate(codeRuleField.getDefaultTemplate())
                                .setSnType(codeRuleField.getDefaultSnType().getKey())
                );
            }
        }
    }

    private void initRootUser() {
        // 初始化用户
        UserEntity userEntity = Services.getUserService().getMaybeNull(1L);
        if (Objects.nonNull(userEntity)) {
            return;
        }
        String salt = Utils.getRandomUtil().randomString(AppConstant.PASSWORD_SALT_LENGTH);
        Services.getUserService().add(new UserEntity()
                .setNickname("Hamm")
                .setAccount("hamm")
                .setPhone("17666666666")
                .setEmail("admin@hamm.cn")
                .setPassword(Utils.getPasswordUtil().encode("Aa123456", salt))
                .setSalt(salt)
                .setRemark("超级管理员,请勿数据库暴力直接删除"));
    }

    @Override
    public void run(String... args) {
        System.out.println(Arrays.toString(args));
        initRootUser();
        initCodeRules();
        initParameters();
        Services.getPermissionService().loadPermission();
        Services.getMenuService().initMenu();
        System.out.println("---------------------------------");
        String[] localEnvList = {"local-hamm"};
        if (Arrays.stream(localEnvList).toList().contains(Utils.getCurrentEnvironment())) {
            initDevData();
        }
    }

    private void initDevData() {
        int deviceCount = 10;
        for (int i = 0; i < deviceCount; i++) {
            Services.getDeviceService().add(new DeviceEntity().setCode("Simulator00" + (i + 1)).setName("设备" + (i + 1)));
        }

        UnitEntity unit = new UnitEntity();
        unit.setName("台");
        unit = Services.getUnitService().get(Services.getUnitService().add(unit));

        MaterialEntity material = new MaterialEntity()
                .setMaterialType(MaterialType.PURCHASE.getKey())
                .setName("MacBook Pro M3 Max")
                .setSpc("32G-1TB")
                .setUnitInfo(unit)
                .setPurchasePrice(28000D)
                .setSalePrice(29999D);
        material = Services.getMaterialService().get(Services.getMaterialService().add(material));

        CustomerEntity customer = new CustomerEntity();
        customer.setName("腾讯科技").setPhone("17666666666");
        customer = Services.getCustomerService().get(Services.getCustomerService().add(customer));

        SupplierEntity supplier = new SupplierEntity();
        supplier.setName("Apple中国").setPhone("17666666666");
        supplier = Services.getSupplierService().get(Services.getSupplierService().add(supplier));

        SalePriceEntity salePrice = new SalePriceEntity();
        salePrice.setCustomer(customer).setPrice(29999D).setMaterial(material);
        Services.getSalePriceService().get(Services.getSalePriceService().add(salePrice));

        PurchasePriceEntity purchasePrice = new PurchasePriceEntity();
        purchasePrice.setSupplier(supplier).setPrice(28000D).setMaterial(material);
        Services.getPurchasePriceService().get(Services.getPurchasePriceService().add(purchasePrice));

        StorageEntity storage = new StorageEntity().setName("东部大仓");
        storage = Services.getStorageService().get(Services.getStorageService().add(storage));

        for (int i = 0; i < FOUR; i++) {
            Services.getStorageService().add(new StorageEntity()
                    .setParentId(storage.getId())
                    .setName(String.format("东部%s仓", (i + 1)))
            );
        }

        storage = new StorageEntity().setName("西部大仓");
        storage = Services.getStorageService().get(Services.getStorageService().add(storage));

        for (int i = 0; i < FOUR; i++) {
            Services.getStorageService().add(new StorageEntity()
                    .setParentId(storage.getId())
                    .setName(String.format("西部%s仓", (i + 1)))
            );
        }
    }
}
