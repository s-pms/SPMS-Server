package cn.hamm.spms.module.personnel.user;

/**
 * <h1>用户登录方式</h1>
 *
 * @author Hamm
 */
public enum UserLoginType {
    /**
     * ID+密码 账号+密码
     */
    VIA_ACCOUNT_PASSWORD,

    /**
     * 邮箱+验证码
     */
    VIA_EMAIL_CODE,

    /**
     * 手机+验证码
     */
    VIA_PHONE_CODE,
}
