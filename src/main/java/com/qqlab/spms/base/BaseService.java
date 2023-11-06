package com.qqlab.spms.base;

import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.root.RootRepository;
import cn.hamm.airpower.root.RootService;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hutool.core.date.DateTime;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.module.system.coderule.CodeRuleEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import com.qqlab.spms.module.system.coderule.CodeRuleParam;
import com.qqlab.spms.module.system.coderule.CodeRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <h1>基础服务类</h1>
 *
 * @author Hamm https://hamm.cn
 */
public class BaseService<E extends BaseEntity<E>, R extends RootRepository<E>> extends RootService<E, R> {
    @Autowired
    private CodeRuleRepository codeRuleRepository;

    @Override
    protected E beforeSaveToDatabase(E entity) {
        List<Field> fields = ReflectUtil.getFieldList(entity.getClass());
        for (Field field : fields) {
            AutoGenerateCode autoGenerateCode = field.getAnnotation(AutoGenerateCode.class);
            if (Objects.isNull(autoGenerateCode)) {
                continue;
            }
            field.setAccessible(true);
            try {
                if (Objects.isNull(field.get(entity))) {
                    String code = this.createCode(autoGenerateCode.value());
                    field.set(entity, code);
                    break;
                }
            } catch (IllegalAccessException e) {
                Result.ERROR.show("生成编码规则失败，反射字段出现异常");
            }
        }
        return entity;
    }

    /**
     * 创建一个自定义编码
     *
     * @param codeRuleField 为哪个字段创建
     * @return 一个自定义编码
     */
    protected String createCode(CodeRuleField codeRuleField) {
        CodeRuleEntity codeRuleEntity = codeRuleRepository.getByRuleField(codeRuleField.getValue());
        Result.ERROR.whenNull(codeRuleEntity, "保存失败,请先配置自定义编码规则!");
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
        codeRuleRepository.saveAndFlush(codeRuleEntity);
        return codeRuleEntity.getPrefix() + template + String.format("%0" + codeRuleEntity.getSnLength() + "d", serialNumber);
    }
}
