package cn.hamm.spms.module.open.oauth;

/**
 * <h1>Oauth动作</h1>
 *
 * @author Hamm.cn
 */
public interface IOauthAction {
    /**
     * <h3>{@code AccessToken} 必须</h3>
     */
    interface WhenAccessTokenRequired {
    }

    /**
     * <h3>{@code AppKey} 必须</h3>
     */
    interface WhenAppKeyRequired {
    }
}
