package com.qqlab.spms.cron;

import cn.hamm.airpower.config.GlobalConfig;
import cn.hamm.airpower.query.QueryRequest;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.qqlab.spms.module.system.coderule.CodeRuleEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleService;
import com.qqlab.spms.module.system.coderule.SerialNumberUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <h1>系统定时任务</h1>
 *
 * @author Hamm
 */
@Component
public class SystemCron {
    @Autowired
    private CodeRuleService codeRuleService;

    @Scheduled(cron = "59 59 23 * * *")
    void softShutdownService() {
        GlobalConfig.isServiceRunning = false;
    }

    @Scheduled(cron = "0 0 0 * * *")
    void resetCodeRuleBaseNumber() {
        List<CodeRuleEntity> codeRules = codeRuleService.getList(new QueryRequest<>());
        for (CodeRuleEntity codeRule : codeRules) {
            resetSn(codeRule);
        }
        GlobalConfig.isServiceRunning = true;
    }

    /**
     * 重置自定义编码规则序列号
     *
     * @param codeRule 编码规则
     */
    private void resetSn(CodeRuleEntity codeRule) {
        DateTime dateTime = DateUtil.date();
        int month = dateTime.monthBaseOne();
        int day = dateTime.dayOfMonth();
        if (codeRule.getSnType().equals(SerialNumberUpdate.YEAR.getValue()) && month == 1 && day == 1) {
            // 按年更新 且是1月1号
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
            return;
        }
        if (codeRule.getSnType().equals(SerialNumberUpdate.MONTH.getValue()) && day == 1) {
            // 按月更新 且是1号
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
            return;
        }
        if (codeRule.getSnType().equals(SerialNumberUpdate.DAY.getValue())) {
            // 按日更新 直接更新
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
        }
    }
}
