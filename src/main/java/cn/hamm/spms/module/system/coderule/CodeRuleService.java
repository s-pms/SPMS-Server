package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.dictionary.DictionaryUtil;
import cn.hamm.spms.base.BaseService;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_DELETE;
import static cn.hamm.airpower.exception.ServiceError.SERVICE_ERROR;
import static cn.hamm.spms.module.system.coderule.CodeRuleParam.*;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class CodeRuleService extends BaseService<CodeRuleEntity, CodeRuleRepository> {
    /**
     * {@code Label}
     */
    public static final String STRING_LABEL = "label";
    /**
     * 年份位数
     */
    private static final int SHORT_YEAR_LENGTH = 2;

    /**
     * 流水号位数
     */
    private static final String CODE_RULE_FORMATTER = "%02d";

    /**
     * 创建一个自定义编码
     *
     * @param codeRuleField 为哪个字段创建
     * @return 一个自定义编码
     */
    public final @NotNull String createCode(@NotNull CodeRuleField codeRuleField) {
        CodeRuleEntity codeRule = repository.getByRuleField(codeRuleField.getKey());
        SERVICE_ERROR.whenNull(codeRule, "保存失败,请先配置自定义编码规则!");
        String template = codeRule.getTemplate();
        List<Map<String, Object>> mapList = DictionaryUtil.getDictionaryList(CodeRuleParam.class);
        Calendar calendar = Calendar.getInstance();
        for (val map : mapList) {
            String param = map.get(STRING_LABEL).toString();
            if (FULL_YEAR.getLabel().equals(param)) {
                template = template.replaceAll(param, String.valueOf(calendar.get(Calendar.YEAR)));
                continue;
            }
            if (YEAR.getLabel().equals(param)) {
                String fullYear = String.valueOf(calendar.get(Calendar.YEAR));
                template = template.replaceAll(param, fullYear.substring(SHORT_YEAR_LENGTH));
                continue;
            }
            if (MONTH.getLabel().equals(param)) {
                template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, calendar.get(Calendar.MONTH) + 1));
                continue;
            }
            if (DATE.getLabel().equals(param)) {
                template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, calendar.get(Calendar.DAY_OF_MONTH)));
                continue;
            }
            if (HOUR.getLabel().equals(param)) {
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
     * 根据规则字段获取自定义编码规则
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
        FORBIDDEN_DELETE.when(codeRule.getIsSystem(), "内置编码规则不能删除!");
    }
}
