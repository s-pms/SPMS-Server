package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.core.DateTimeUtil;
import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleField;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleParam;
import cn.hamm.spms.module.system.coderule.enums.SerialNumberUpdate;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_DELETE;
import static cn.hamm.airpower.exception.Errors.SERVICE_ERROR;
import static cn.hamm.spms.module.system.coderule.enums.CodeRuleParam.*;

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
        AtomicReference<String> code = new AtomicReference<>("");
        transactionHelper.run(() -> {
            CodeRuleEntity forUpdate = getForUpdate(codeRule.getId());
            SerialNumberUpdate serialNumberUpdate = DictionaryUtil.getDictionary(SerialNumberUpdate.class, forUpdate.getSnType());
            final int currentYear = DateTimeUtil.getCurrentYear();
            final int currentMonth = DateTimeUtil.getCurrentMonth();
            final int currentDay = DateTimeUtil.getCurrentDay();
            switch (serialNumberUpdate) {
                case YEAR -> {
                    if (forUpdate.getCurrentYear() != currentYear) {
                        forUpdate.setCurrentYear(currentYear)
                                .setCurrentSn(0);
                    }
                }
                case MONTH -> {
                    if (forUpdate.getCurrentMonth() != currentMonth) {
                        forUpdate
                                .setCurrentYear(currentYear)
                                .setCurrentMonth(currentMonth)
                                .setCurrentSn(0);
                    }
                }
                case DAY -> {
                    if (forUpdate.getCurrentDay() != currentDay) {
                        forUpdate
                                .setCurrentYear(currentYear)
                                .setCurrentMonth(currentMonth)
                                .setCurrentDay(currentDay)
                                .setCurrentSn(0);
                    }
                }
                default -> {
                }
            }
            String template = forUpdate.getTemplate();
            List<Map<String, Object>> mapList = DictionaryUtil.getDictionaryList(CodeRuleParam.class);
            for (val map : mapList) {
                String param = map.get(STRING_LABEL).toString();
                if (FULL_YEAR.getLabel().equals(param)) {
                    template = template.replaceAll(param, String.valueOf(currentYear));
                    continue;
                }
                if (YEAR.getLabel().equals(param)) {
                    String fullYear = String.valueOf(currentYear);
                    template = template.replaceAll(param, fullYear.substring(SHORT_YEAR_LENGTH));
                    continue;
                }
                if (MONTH.getLabel().equals(param)) {
                    template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, currentMonth));
                    continue;
                }
                if (DATE.getLabel().equals(param)) {
                    template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, currentDay));
                    continue;
                }
                if (HOUR.getLabel().equals(param)) {
                    template = template.replaceAll(param, String.format(CODE_RULE_FORMATTER, DateTimeUtil.getCurrentHour()));
                }
            }
            int serialNumber = forUpdate.getCurrentSn();
            serialNumber++;
            forUpdate.setCurrentSn(serialNumber);
            repository.saveAndFlush(forUpdate);
            code.set(forUpdate.getPrefix() + template + String.format("%0" + forUpdate.getSnLength() + "d", serialNumber));
        });
        return code.get();
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
    protected void beforeDelete(@NotNull CodeRuleEntity codeRule) {
        FORBIDDEN_DELETE.when(codeRule.getIsSystem(), "内置编码规则不能删除!");
    }
}
