package cn.hamm.spms.common.cron;

import cn.hamm.airpower.config.Configs;
import cn.hamm.airpower.model.query.QueryListRequest;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.system.coderule.CodeRuleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import cn.hamm.spms.module.system.coderule.SerialNumberUpdate;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

/**
 * <h1>系统定时任务</h1>
 *
 * @author Hamm.cn
 */
@Component
public class ServiceCron {

    @Scheduled(cron = "59 59 23 * * *")
    void softShutdownService() {
        Configs.getServiceConfig().setServiceRunning(false);
    }

    @Scheduled(cron = "0 0 0 * * *")
    void resetCodeRuleBaseNumber() {
        List<CodeRuleEntity> codeRules = Services.getCodeRuleService().getList(new QueryListRequest<>());
        codeRules.forEach(this::resetSn);
        Configs.getServiceConfig().setServiceRunning(true);
    }

    /**
     * <h2>重置自定义编码规则序列号</h2>
     *
     * @param codeRule 编码规则
     */
    private void resetSn(@NotNull CodeRuleEntity codeRule) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        CodeRuleService codeRuleService = Services.getCodeRuleService();
        if (codeRule.getSnType().equals(SerialNumberUpdate.YEAR.getKey()) && month == 1 && day == 1) {
            // 按年更新 且是1月1号
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
            return;
        }
        if (codeRule.getSnType().equals(SerialNumberUpdate.MONTH.getKey()) && day == 1) {
            // 按月更新 且是1号
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
            return;
        }
        if (codeRule.getSnType().equals(SerialNumberUpdate.DAY.getKey())) {
            // 按日更新 直接更新
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
        }
    }
}
