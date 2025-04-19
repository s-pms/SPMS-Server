package cn.hamm.spms.module.personnel.user;

/**
 * <h1>用户行为</h1>
 *
 * @author Hamm.cn
 */
public interface IUserAction {

    /**
     * ID密码 密码登录
     */
    interface WhenLogin {
    }

    /**
     * 邮箱验证码登录
     */
    interface WhenLoginViaEmail {
    }

    /**
     * 密码重置
     */
    interface WhenResetMyPassword {
    }

    /**
     * 修改密码
     */
    interface WhenUpdateMyPassword {
    }

    /**
     * 修改资料
     */
    interface WhenUpdateMyInfo {
    }

    /**
     * 发送邮件
     */
    interface WhenSendEmail {
    }

    /**
     * 发送短信
     */
    interface WhenSendSms {
    }

    /**
     * 获取我的信息
     */
    interface WhenGetMyInfo {
    }
}
