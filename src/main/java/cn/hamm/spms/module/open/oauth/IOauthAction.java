package cn.hamm.spms.module.open.oauth;

/**
 * <h1>Oauth 动作</h1>
 *
 * @author Hamm.cn
 */
public interface IOauthAction {
    /**
     * {@code AccessToken} 必须
     */
    interface WhenAccessTokenRequired {
    }

    /**
     * {@code AppKey} 必须
     */
    interface WhenAppKeyRequired {
    }
}
