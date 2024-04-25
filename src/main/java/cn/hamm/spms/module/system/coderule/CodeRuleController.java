package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.model.json.JsonData;
import cn.hamm.airpower.util.AirUtil;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("coderule")
@Description("编码规则")
public class CodeRuleController extends BaseController<CodeRuleEntity, CodeRuleService, CodeRuleRepository> {
    @Description("获取支持的表")
    @Permission(login = false)
    @RequestMapping("getFieldList")
    public JsonData getFieldList() {
        return jsonData(AirUtil.getDictionaryUtil().getDictionaryList(CodeRuleField.class, "key", "label", "defaultPrefix"));
    }

    @Description("获取支持的参数")
    @Permission(login = false)
    @RequestMapping("getParamList")
    public JsonData getParamList() {
        return jsonData(AirUtil.getDictionaryUtil().getDictionaryList(CodeRuleParam.class, "key", "label", "desc", "demo"));
    }

    @Description("获取流水号更新方式")
    @Permission(login = false)
    @RequestMapping("getSerialNumberUpdate")
    public JsonData getSerialNumberUpdate() {
        return jsonData(AirUtil.getDictionaryUtil().getDictionaryList(SerialNumberUpdate.class));
    }
}
