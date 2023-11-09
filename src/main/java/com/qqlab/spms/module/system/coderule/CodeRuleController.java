package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.security.Permission;
import cn.hamm.airpower.util.ReflectUtil;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("coderule")
@Description("编码规则")
public class CodeRuleController extends BaseController<CodeRuleService, CodeRuleEntity> {
    @Description("获取支持的表")
    @Permission(login = false)
    @PostMapping("getFieldList")
    public JsonData getFieldList() {
        return jsonData(ReflectUtil.getEnumMapList(CodeRuleField.class, "value", "label", "defaultPrefix"));
    }

    @Description("获取支持的参数")
    @Permission(login = false)
    @PostMapping("getParamList")
    public JsonData getParamList() {
        return jsonData(ReflectUtil.getEnumMapList(CodeRuleParam.class, "value", "label", "desc", "demo"));
    }

    @Description("获取流水号更新方式")
    @Permission(login = false)
    @PostMapping("getSerialNumberUpdate")
    public JsonData getSerialNumberUpdate() {
        return jsonData(ReflectUtil.getEnumMapList(SerialNumberUpdate.class));
    }
}
