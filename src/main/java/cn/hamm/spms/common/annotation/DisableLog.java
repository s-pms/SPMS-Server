package cn.hamm.spms.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <h1>禁止记录日志</h1>
 *
 * @author Hamm.cn
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface DisableLog {
}
