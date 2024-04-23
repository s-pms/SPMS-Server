package cn.hamm.spms.module.system.app;

/**
 * <h1>应用动作</h1>
 *
 * @author Hamm.cn
 */
public interface IAppAction {
    /**
     * <h2>获取AccessToken</h2>
     */
    interface WhenAccessToken {
    }

    /**
     * <h2>重置密钥</h2>
     */
    interface WhenResetSecret {
    }

    /**
     * <h2>通过Code获取AccessToken</h2>
     */
    interface WhenCode2AccessToken {
    }

    /**
     * <h2>通过AppKey获取应用信息</h2>
     */
    interface WhenGetByAppKey {
    }
}
