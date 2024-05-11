package cn.hamm.spms.common.exception;

import cn.hamm.airpower.interfaces.IException;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

/**
 * <h1>应用自定义异常代码</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
public enum CustomError implements IException {
    EMAIL_SEND_BUSY(1, "发送邮件过于频繁，请稍后再试"),
    USER_LOGIN_ACCOUNT_OR_PASSWORD_INVALID(2, "用户账号或密码错误"),
    USER_REGISTER_ERROR_EXIST(3, "注册失败，账号已存在"),

    ;

    private static final int BASE_CODE = 10000000;
    private final int code;
    private final String message;

    @Contract(pure = true)
    CustomError(int code, String message) {
        this.code = code + BASE_CODE;
        this.message = message;
    }
}
