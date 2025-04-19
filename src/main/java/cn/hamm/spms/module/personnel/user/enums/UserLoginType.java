package cn.hamm.spms.module.personnel.user.enums;

/**
 * <h1>用户登录方式</h1>
 *
 * @author Hamm.cn
 */
public enum UserLoginType {
    /**
     * ID+密码 邮箱+密码
     */
    VIA_ACCOUNT_PASSWORD,

    /**
     * 邮箱+验证码
     */
    VIA_EMAIL_CODE,
}
