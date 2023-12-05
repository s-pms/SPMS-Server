package com.qqlab.spms.annotation;

import com.qqlab.spms.module.system.coderule.CodeRuleField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>自动生成编码</h1>
 *
 * @author Hamm
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoGenerateCode {
    /**
     * 使用的自定义编码规则枚举项
     */
    CodeRuleField value();
}
