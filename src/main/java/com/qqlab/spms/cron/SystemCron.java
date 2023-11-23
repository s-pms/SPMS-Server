package com.qqlab.spms.cron;

import cn.hamm.airpower.config.GlobalConfig;
import cn.hamm.airpower.query.QueryRequest;
import com.qqlab.spms.module.system.coderule.CodeRuleEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <h1>系统定时任务</h1>
 *
 * @author hamm
 */
@Component
public class SystemCron {
    @Autowired
    private CodeRuleService codeRuleService;

    @Scheduled(cron = "59 59 23 * * *")
    void resetCodeRuleBaseNumber() throws InterruptedException {
        GlobalConfig.isServiceRunning = false;
        List<CodeRuleEntity> codeRules = codeRuleService.getList(new QueryRequest<>());
        for (CodeRuleEntity codeRule : codeRules) {
            codeRule.setCurrentSn(0);
            codeRuleService.update(codeRule);
        }
        Thread.sleep(1000);
        GlobalConfig.isServiceRunning = true;
    }
}
