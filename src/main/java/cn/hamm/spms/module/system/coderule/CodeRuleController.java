package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.curd.permission.Permission;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleField;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleParam;
import cn.hamm.spms.module.system.coderule.enums.SerialNumberUpdate;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("coderule")
@Description("编码规则")
public class CodeRuleController extends BaseController<CodeRuleEntity, CodeRuleService, CodeRuleRepository> {
    @Description("获取支持的表")
    @Permission(login = false)
    @PostMapping("getFieldList")
    public Json getFieldList() {
        return Json.data(DictionaryUtil.getDictionaryList(CodeRuleField.class,
                CodeRuleField::getKey,
                CodeRuleField::getLabel,
                CodeRuleField::getDefaultPrefix
        ));
    }

    @Description("获取支持的参数")
    @Permission(login = false)
    @PostMapping("getParamList")
    public Json getParamList() {
        return Json.data(DictionaryUtil.getDictionaryList(CodeRuleParam.class,
                CodeRuleParam::getKey,
                CodeRuleParam::getLabel,
                CodeRuleParam::getDesc,
                CodeRuleParam::getDemo
        ));
    }

    @Description("获取流水号更新方式")
    @Permission(login = false)
    @PostMapping("getSerialNumberUpdate")
    public Json getSerialNumberUpdate() {
        return Json.data(DictionaryUtil.getDictionaryList(SerialNumberUpdate.class));
    }
}
