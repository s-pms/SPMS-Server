package cn.hamm.spms.module.personnel.user;

/**
 * <h1>用户登录方式</h1>
 *
 * @author Hamm.cn
 */
public enum UserLoginType {
    /**
     * <h3>ID+密码 账号+密码</h3>
     */
    VIA_ACCOUNT_PASSWORD,

    /**
     * <h3>邮箱+验证码</h3>
     */
    VIA_EMAIL_CODE,

    /**
     * <h3>手机+验证码</h3>
     */
    VIA_PHONE_CODE,
}
