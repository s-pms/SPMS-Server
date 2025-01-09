package cn.hamm.spms.module.open.oauth.model.base;

/**
 * <h1>OAuth2回调抽象类</h1>
 *
 * @author Hamm.cn
 */
public abstract class AbstractOauthCallback {
    /**
     * <h3>获取用户信息</h3>
     *
     * @param code 临时Code
     * @return 用户信息
     */
    public abstract OauthUserInfo getUserInfo(String code);
}
