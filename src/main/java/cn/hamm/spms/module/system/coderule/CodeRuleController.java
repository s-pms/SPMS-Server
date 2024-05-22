package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("coderule")
@Description("编码规则")
public class CodeRuleController extends BaseController<CodeRuleEntity, CodeRuleService, CodeRuleRepository> {

    @Description("获取支持的表")
    @Permission(login = false)
    @RequestMapping("getFieldList")
    public Json getFieldList() {
        return Json.data(Utils.getDictionaryUtil().getDictionaryList(CodeRuleField.class,
                CodeRuleField::getKey,
                CodeRuleField::getLabel,
                CodeRuleField::getDefaultPrefix
        ));
    }

    @Description("获取支持的参数")
    @Permission(login = false)
    @RequestMapping("getParamList")
    public Json getParamList() {
        return Json.data(Utils.getDictionaryUtil().getDictionaryList(CodeRuleParam.class,
                CodeRuleParam::getKey,
                CodeRuleParam::getLabel,
                CodeRuleParam::getDesc,
                CodeRuleParam::getDemo
        ));
    }

    @Description("获取流水号更新方式")
    @Permission(login = false)
    @RequestMapping("getSerialNumberUpdate")
    public Json getSerialNumberUpdate() {
        return Json.data(Utils.getDictionaryUtil().getDictionaryList(SerialNumberUpdate.class));
    }
}
