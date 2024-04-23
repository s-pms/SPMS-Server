package cn.hamm.spms.common.annotation;

import cn.hamm.spms.module.system.coderule.CodeRuleField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>自动生成编码</h1>
 *
 * @author Hamm.cn
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoGenerateCode {
    /**
     * <h2>使用的自定义编码规则枚举项</h2>
     */
    CodeRuleField value();
}
