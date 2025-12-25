package cn.hamm.spms.common.cron;

import cn.hamm.spms.module.system.coderule.CodeRuleEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

import static cn.hamm.spms.module.system.coderule.enums.SerialNumberUpdate.*;

/**
 * <h1>系统定时任务</h1>
 *
 * @author Hamm.cn
 */
@Component
public class ServiceCron {

    @Autowired
    private CodeRuleService codeRuleService;

    @Scheduled(cron = "59 59 23 * * *")
    void softShutdownService() {
    }

    @Scheduled(cron = "0 0 0 * * *")
    void resetCodeRuleBaseNumber() {
        List<CodeRuleEntity> codeRules = codeRuleService.filter(null);
        codeRules.forEach(this::resetSn);
    }

    /**
     * 重置自定义编码规则序列号
     *
     * @param codeRule 编码规则
     */
    private void resetSn(@NotNull CodeRuleEntity codeRule) {
        codeRuleService.updateWithLock(codeRule.getId(), exist -> {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (exist.getSnType().equals(YEAR.getKey()) && month == 1 && day == 1) {
                // 按年更新 且是1月1号
                exist.setCurrentSn(0);
                return;
            }
            if (exist.getSnType().equals(MONTH.getKey()) && day == 1) {
                // 按月更新 且是1号
                exist.setCurrentSn(0);
                return;
            }
            if (exist.getSnType().equals(DAY.getKey())) {
                // 按日更新 直接更新
                exist.setCurrentSn(0);
            }
        });
    }
}
