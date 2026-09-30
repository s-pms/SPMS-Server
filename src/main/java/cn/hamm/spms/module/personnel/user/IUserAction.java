package cn.hamm.spms.module.personnel.user;

/**
 * <h1>用户的校验分组</h1>
 *
 * @author Hamm.cn
 */
public interface IUserAction {

    /**
     * 账号密码登录时的校验分组
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
     * 获取我的信息时的校验分组
     */
    interface WhenGetMyInfo {
    }
}
