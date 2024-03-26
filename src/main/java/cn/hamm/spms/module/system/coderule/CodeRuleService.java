package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.spms.base.BaseService;
import cn.hutool.core.date.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Hamm
 */
@Service
public class CodeRuleService extends BaseService<CodeRuleEntity, CodeRuleRepository> {
    /**
     * 创建一个自定义编码
     *
     * @param codeRuleField 为哪个字段创建
     * @return 一个自定义编码
     */
    public final String createCode(CodeRuleField codeRuleField) {
        CodeRuleEntity codeRuleEntity = repository.getByRuleField(codeRuleField.getKey());
        Result.ERROR.whenNull(codeRuleEntity, "保存失败,请先配置自定义编码规则!");
        String template = codeRuleEntity.getTemplate();
        List<Map<String, String>> mapList = DictionaryUtil.getDictionaryList(CodeRuleParam.class);
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
                template = template.replaceAll(param, String.format("%02d", DateTime.now().monthBaseOne()));
                continue;
            }
            if (CodeRuleParam.DATE.getLabel().equals(param)) {
                template = template.replaceAll(param, String.format("%02d", DateTime.now().dayOfMonth()));
                continue;
            }
            if (CodeRuleParam.HOUR.getLabel().equals(param)) {
                template = template.replaceAll(param, String.format("%02d", DateTime.now().hour(true)));
            }
        }
        int serialNumber = codeRuleEntity.getCurrentSn();
        serialNumber++;
        codeRuleEntity.setCurrentSn(serialNumber);
        repository.saveAndFlush(codeRuleEntity);
        return codeRuleEntity.getPrefix() + template + String.format("%0" + codeRuleEntity.getSnLength() + "d", serialNumber);
    }
}
