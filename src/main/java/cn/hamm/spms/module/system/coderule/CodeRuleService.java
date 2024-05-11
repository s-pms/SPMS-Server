package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class CodeRuleService extends BaseService<CodeRuleEntity, CodeRuleRepository> {
    private static final int SHORT_YEAR_LENGTH = 2;
    private static final String CODE_RULE_FORMATTER = "%02d";

    /**
     * <h2>创建一个自定义编码</h2>
     *
     * @param codeRuleField 为哪个字段创建
     * @return 一个自定义编码
     */
    public final @NotNull String createCode(@NotNull CodeRuleField codeRuleField) {
        CodeRuleEntity codeRule = repository.getByRuleField(codeRuleField.getKey());
        ServiceError.SERVICE_ERROR.whenNull(codeRule, "保存失败,请先配置自定义编码规则!");
        String template = codeRule.getTemplate();
        List<Map<String, String>> mapList = Utils.getDictionaryUtil().getDictionaryList(CodeRuleParam.class);
        Calendar calendar = Calendar.getInstance();
        for (Map<String, String> map : mapList) {
            String param = map.get(Constant.LABEL);
            if (CodeRuleParam.FULL_YEAR.getLabel().equals(param)) {
                template = template.replaceAll(param, String.valueOf(calendar.get(Calendar.YEAR)));
                continue;
            }
            if (CodeRuleParam.YEAR.getLabel().equals(param)) {
                String fullYear = String.valueOf(calendar.get(Calendar.YEAR));
                template = template.replaceAll(param, fullYear.substring(SHORT_YEAR_LENGTH));
                continue;
            }
            if (CodeRuleParam.MONTH.getLabel().equals(param)) {
                template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, calendar.get(Calendar.MONTH) + 1));
                continue;
            }
            if (CodeRuleParam.DATE.getLabel().equals(param)) {
                template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, calendar.get(Calendar.DAY_OF_MONTH)));
                continue;
            }
            if (CodeRuleParam.HOUR.getLabel().equals(param)) {
                template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, calendar.get(Calendar.HOUR_OF_DAY)));
            }
        }
        int serialNumber = codeRule.getCurrentSn();
        serialNumber++;
        codeRule.setCurrentSn(serialNumber);
        repository.saveAndFlush(codeRule);
        return codeRule.getPrefix() + template + String.format("%0" + codeRule.getSnLength() + "d", serialNumber);
    }

    /**
     * <h2>根据规则字段获取自定义编码规则</h2>
     *
     * @param ruleField 规则字段
     * @return 自定义编码规则
     */
    public final CodeRuleEntity getByRuleField(Integer ruleField) {
        return repository.getByRuleField(ruleField);
    }

    @Override
    protected void beforeDelete(long id) {
        CodeRuleEntity codeRule = get(id);
        ServiceError.FORBIDDEN_DELETE.when(codeRule.getIsSystem(), "内置编码规则不能删除!");
    }
}
