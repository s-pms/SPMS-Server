package cn.hamm.spms.module.open.oauth;

/**
 * <h1>第三方授权动作</h1>
 *
 * @author Hamm.cn
 */
public interface IOauthAction {
    /**
     * {@code AccessToken} 必填时的参数校验组
     */
    interface WhenAccessTokenRequired {
    }

    /**
     * {@code AppKey} 必填时的参数校验组
     */
    interface WhenAppKeyRequired {
    }
}
