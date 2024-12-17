package cn.hamm.spms.module.personnel.user;

/**
 * <h1>动作</h1>
 *
 * @author Hamm.cn
 */
public interface IUserAction {
    /**
     * <h3>ID+密码 邮箱+密码登录</h3>
     */
    interface WhenLogin {
    }

    /**
     * <h3>邮箱+验证码登录</h3>
     */
    interface WhenLoginViaEmail {
    }

    /**
     * <h3>手机+验证码登录</h3>
     */
    interface WhenLoginViaPhone {
    }

    /**
     * <h3>修改我的密码</h3>
     */
    interface WhenUpdateMyPassword {
    }

    /**
     * <h3>修改我的资料</h3>
     */
    interface WhenUpdateMyInfo {
    }

    /**
     * <h3>发送邮件</h3>
     */
    interface WhenSendEmail {
    }

    /**
     * <h3>发送短信</h3>
     */
    interface WhenSendMessage {
    }

    /**
     * <h3>获取我的个人信息</h3>
     */
    interface WhenGetMyInfo {
    }
}
