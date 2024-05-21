package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("coderule")
@Description("编码规则")
public class CodeRuleController extends BaseController<CodeRuleEntity, CodeRuleService, CodeRuleRepository> {
    public static final String DEFAULT_PREFIX = "defaultPrefix";
    public static final String DESC = "desc";
    public static final String DEMO = "demo";

    @Description("获取支持的表")
    @Permission(login = false)
    @RequestMapping("getFieldList")
    public Json getFieldList() {
        return Json.data(Utils.getDictionaryUtil().getDictionaryList(CodeRuleField.class, Constant.KEY, Constant.LABEL, DEFAULT_PREFIX));
    }

    @Description("获取支持的参数")
    @Permission(login = false)
    @RequestMapping("getParamList")
    public Json getParamList() {
        return Json.data(Utils.getDictionaryUtil().getDictionaryList(CodeRuleParam.class, Constant.KEY, Constant.LABEL, DESC, DEMO));
    }

    @Description("获取流水号更新方式")
    @Permission(login = false)
    @RequestMapping("getSerialNumberUpdate")
    public Json getSerialNumberUpdate() {
        return Json.data(Utils.getDictionaryUtil().getDictionaryList(SerialNumberUpdate.class));
    }
}
