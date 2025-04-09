package cn.hamm.spms.common.exception;

import cn.hamm.airpower.exception.IException;
import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.spms.common.config.AppConstant;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

/**
 * <h1>应用自定义异常代码</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
public enum CustomError implements IException<CustomError>, IDictionary {
    USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID(1, "用户账号或密码错误"),

    EMAIL_SEND_BUSY(101, "发送邮件过于频繁，请稍后再试"),
    SMS_SEND_BUSY(102, "发送短信过于频繁，请稍后再试"),
    ;

    private final int code;
    private final String message;

    @Contract(pure = true)
    CustomError(int code, String message) {
        this.code = code + AppConstant.BASE_CUSTOM_ERROR;
        this.message = message;
    }

    /**
     * <h3>获取枚举的 {@code Key}</h3>
     *
     * @return {@code Key}
     */
    @Contract(pure = true)
    @Override
    public int getKey() {
        return code;
    }

    /**
     * <h3>获取枚举的描述</h3>
     *
     * @return 描述
     */
    @Contract(pure = true)
    @Override
    public String getLabel() {
        return message;
    }
}
