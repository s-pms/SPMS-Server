package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.Permission;
import cn.hamm.airpower.util.ReflectUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CodeRuleController extends RootEntityController<CodeRuleService, CodeRuleEntity> {
    @Autowired
    private CodeRuleService codeRuleService;

    @Description("获取支持的表")
    @Permission(login = false)
    @PostMapping("getTableList")
    public JsonData getTableList() {
        return jsonData(ReflectUtil.getEnumMapList(CodeRuleTable.class, "value", "label", "table", "defaultPrefix"));
    }

    @Description("获取支持的参数")
    @Permission(login = false)
    @PostMapping("getParams")
    public JsonData getParams() {
        return jsonData(ReflectUtil.getEnumMapList(CodeRuleParam.class, "value", "label", "desc", "demo"));
    }

    @Description("获取流水号更新方式")
    @Permission(login = false)
    @PostMapping("getSerialNumberUpdate")
    public JsonData getSerialNumberUpdate() {
        return jsonData(ReflectUtil.getEnumMapList(SerialNumberUpdate.class));
    }
}
