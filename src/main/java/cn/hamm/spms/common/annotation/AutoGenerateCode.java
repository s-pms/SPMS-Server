package cn.hamm.spms.common.annotation;

import cn.hamm.spms.module.system.coderule.enums.CodeRuleField;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <h1>自动生成编码</h1>
 *
 * @author Hamm.cn
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface AutoGenerateCode {
    /**
     * 使用的自定义编码规则枚举项
     */
    CodeRuleField value();
}
