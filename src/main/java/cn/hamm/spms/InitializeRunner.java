package cn.hamm.spms;

import cn.hamm.airpower.util.AirUtil;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.report.ReportDataType;
import cn.hamm.spms.module.iot.report.ReportEvent;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
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
        String salt = AirUtil.getRandomUtil().randomString();
        Services.getUserService().add(new UserEntity()
                .setNickname("Hamm")
                .setAccount("hamm")
                .setPhone("17623205062")
                .setEmail("admin@hamm.cn")
                .setPassword(AirUtil.getPasswordUtil().encode("Aa123456", salt))
                .setSalt(salt)
                .setRemark("超级管理员,请勿数据库暴力直接删除"));
    }

    @Override
    public void run(String... args) {
        System.out.println(Arrays.toString(args));
        initRootUser();
        initCodeRules();
        initParameters();
        Services.getPermissionService().initPermission("cn.hamm.spms");
        Services.getMenuService().initMenu();
        System.out.println("---------------------------------");
        String[] localEnvList = {"hamm"};
        if (Arrays.stream(localEnvList).toList().contains(AirUtil.getCurrentEnvironment())) {
            initDevData();
        }
    }

    private void initDevData() {
        int deviceCount = 10;
        for (int i = 0; i < deviceCount; i++) {
            Services.getDeviceService().add(new DeviceEntity().setCode("Simulator00" + (i + 1)).setName("设备" + (i + 1)));
        }
    }
}
