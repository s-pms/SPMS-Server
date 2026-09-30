package cn.hamm.spms.module.open.oauth.model.base;

/**
 * <h1>第三方平台回调</h1>
 *
 * @author Hamm.cn
 */
public abstract class AbstractOauthCallback {
    /**
     * 获取用户信息
     *
     * @param code 临时 Code
     * @return 用户信息
     */
    public abstract OauthUserInfo getUserInfo(String code);
}
