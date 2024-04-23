package cn.hamm.spms.module.personnel.user;

/**
 * <h1>用户登录方式</h1>
 *
 * @author Hamm.cn
 */
public enum UserLoginType {
    /**
     * <h2>ID+密码 账号+密码</h2>
     */
    VIA_ACCOUNT_PASSWORD,

    /**
     * <h2>邮箱+验证码</h2>
     */
    VIA_EMAIL_CODE,

    /**
     * <h2>手机+验证码</h2>
     */
    VIA_PHONE_CODE,
}
