package cn.hamm.spms.module.personnel.user;

/**
 * <h1>动作</h1>
 *
 * @author Hamm
 */
public interface IUserAction {
    /**
     * <h2>ID+密码 邮箱+密码登录</h2>
     */
    interface WhenLogin {
    }

    /**
     * <h2>邮箱+验证码登录</h2>
     */
    interface WhenLoginViaEmail {
    }

    /**
     * <h2>手机+验证码登录</h2>
     */
    interface WhenLoginViaPhone {
    }

    /**
     * <h2>修改我的密码</h2>
     */
    interface WhenUpdateMyPassword {
    }

    /**
     * <h2>修改我的资料</h2>
     */
    interface WhenUpdateMyInfo {
    }

    /**
     * <h2>发送邮件</h2>
     */
    interface WhenSendEmail {
    }

    /**
     * <h2>发送短信</h2>
     */
    interface WhenSendMessage {
    }

    /**
     * <h2>获取我的个人信息</h2>
     */
    interface WhenGetMyInfo {
    }
}
