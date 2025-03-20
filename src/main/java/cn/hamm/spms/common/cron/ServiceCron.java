package cn.hamm.spms.common.cron;

import cn.hamm.airpower.web.config.WebConfig;
import cn.hamm.spms.module.system.coderule.CodeRuleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

import static cn.hamm.spms.module.system.coderule.SerialNumberUpdate.*;

/**
 * <h1>系统定时任务</h1>
 *
 * @author Hamm.cn
 */
@Component
public class ServiceCron {
    @Autowired
    private WebConfig webConfig;

    @Autowired
    private CodeRuleService codeRuleService;

    @Scheduled(cron = "59 59 23 * * *")
    void softShutdownService() {
        webConfig.setServiceRunning(false);
    }

    @Scheduled(cron = "0 0 0 * * *")
    void resetCodeRuleBaseNumber() {
        List<CodeRuleEntity> codeRules = codeRuleService.filter(null);
        codeRules.forEach(this::resetSn);
        webConfig.setServiceRunning(true);
    }

    /**
     * <h3>重置自定义编码规则序列号</h3>
     *
     * @param codeRule 编码规则
     */
    private void resetSn(@NotNull CodeRuleEntity codeRule) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (codeRule.getSnType().equals(YEAR.getKey()) && month == 1 && day == 1) {
            // 按年更新 且是1月1号
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
            return;
        }
        if (codeRule.getSnType().equals(MONTH.getKey()) && day == 1) {
            // 按月更新 且是1号
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
            return;
        }
        if (codeRule.getSnType().equals(DAY.getKey())) {
            // 按日更新 直接更新
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
        }
    }
}
