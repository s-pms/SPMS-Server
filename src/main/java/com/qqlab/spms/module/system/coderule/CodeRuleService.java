package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.root.RootService;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hutool.core.date.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Hamm https://hamm.cn
 */
@Service
public class CodeRuleService extends RootService<CodeRuleEntity, CodeRuleRepository> {

    String createCode(CodeRuleTable codeRuleTable) {
        CodeRuleEntity codeRuleEntity = repository.getByTableId(codeRuleTable.getValue());
        Result.ERROR.whenNull(codeRuleEntity, "获取自定义规则失败");
        String template = codeRuleEntity.getTemplate();
        List<Map<String, String>> mapList = ReflectUtil.getEnumMapList(CodeRuleParam.class);
        for (Map<String, String> map : mapList) {
            String param = map.get("label");
            if (CodeRuleParam.FULL_YEAR.getLabel().equals(param)) {
                template = template.replaceAll(param, String.valueOf(DateTime.now().year()));
                continue;
            }
            if (CodeRuleParam.YEAR.getLabel().equals(param)) {
                String fullYear = String.valueOf(DateTime.now().year());
                template = template.replaceAll(param, fullYear.substring(2));
                continue;
            }
            if (CodeRuleParam.MONTH.getLabel().equals(param)) {
                template = template.replaceAll(param, String.valueOf(DateTime.now().monthBaseOne()));
                continue;
            }
            if (CodeRuleParam.DATE.getLabel().equals(param)) {
                template = template.replaceAll(param, String.valueOf(DateTime.now().dayOfMonth()));
                continue;
            }
            if (CodeRuleParam.HOUR.getLabel().equals(param)) {
                template = template.replaceAll(param, String.valueOf(DateTime.now().hour(true)));
            }
        }
        int serialNumber = codeRuleEntity.getCurrentSn();
        serialNumber++;
        codeRuleEntity.setCurrentSn(serialNumber);
        update(codeRuleEntity);
        return codeRuleEntity.getPrefix() + template + String.format("%0" + codeRuleEntity.getSnLength() + "d", serialNumber);
    }
}
