package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.core.DateTimeUtil;
import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.ReflectUtil;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleField;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleParam;
import cn.hamm.spms.module.system.coderule.enums.SerialNumberUpdate;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_DELETE;
import static cn.hamm.airpower.exception.Errors.SERVICE_ERROR;
import static cn.hamm.spms.module.system.coderule.enums.CodeRuleParam.*;

/**
 * <h1>编码规则</h1>
 *
 * @author Hamm.cn
 */
@Service
public class CodeRuleService extends BaseService<CodeRuleEntity, CodeRuleRepository> {
    /**
     * 字典结果里的 label 键名
     */
    public static final String STRING_LABEL = "label";

    /**
     * 两位年份的截取位置
     */
    private static final int SHORT_YEAR_LENGTH = 2;

    /**
     * 月、日、小时的补零格式
     */
    private static final String CODE_RULE_FORMATTER = "%02d";

    /**
     * 生成一个业务编码
     *
     * @param codeRuleField 编码规则字段
     * @return 生成的编码
     * @apiNote 整个过程在一个事务内完成：先锁住规则行再读改写流水号，
     * 否则并发下会拿到重复流水号
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
     * 根据规则字段获取编码规则
     *
     * @param ruleField 规则字段
     * @return 编码规则，未配置时返回 {@code null}
     */
    public final CodeRuleEntity getByRuleField(Integer ruleField) {
        return repository.getByRuleField(ruleField);
    }

    @Override
    protected void beforeDelete(@NotNull CodeRuleEntity codeRule) {
        FORBIDDEN_DELETE.when(codeRule.getIsSystem(), "内置编码规则不能删除!");
    }


    /**
     * 为实体中标注了 {@code @AutoGenerateCode} 且为空的字段填充编码
     *
     * @param entity 实体
     * @param <E>    实体类型
     * @apiNote 命中第一个空字段就 {@code break}：一个实体只允许一个自动编码字段
     */
    public <E extends BaseEntity<E>> void fillFieldAutoCode(@NotNull E entity) {
        List<Field> fields = ReflectUtil.getFieldList(entity.getClass());
        for (Field field : fields) {
            AutoGenerateCode autoGenerateCode = ReflectUtil.getAnnotation(AutoGenerateCode.class, field);
            if (Objects.isNull(autoGenerateCode)) {
                continue;
            }
            Object value = ReflectUtil.getFieldValue(entity, field);
            if (!Objects.isNull(value) && StringUtils.hasText(value.toString())) {
                continue;
            }
            String code = createCode(autoGenerateCode.value());
            ReflectUtil.setFieldValue(entity, field, code);
            break;
        }
    }
}
