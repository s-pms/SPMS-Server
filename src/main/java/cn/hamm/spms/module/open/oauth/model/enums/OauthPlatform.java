package cn.hamm.spms.module.open.oauth.model.enums;

import cn.hamm.airpower.dictionary.IDictionary;
import cn.hamm.spms.module.open.oauth.model.base.AbstractOauthCallback;
import cn.hamm.spms.module.open.oauth.model.platform.WeComCallback;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>OAuth2第三方平台</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum OauthPlatform implements IDictionary {
    /**
     * 企业微信
     */
    WE_COM(1, "企业微信", "wecom", WeComCallback.class);

    private final int key;
    private final String label;
    private final String flag;
    private final Class<? extends AbstractOauthCallback> clazz;
}
