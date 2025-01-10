package cn.hamm.spms.module.open.oauth.model.response;

import cn.hamm.airpower.root.RootModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>{@code AccessToken} 响应对象</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OauthGetAccessTokenResponse extends RootModel<OauthGetAccessTokenResponse> {
    /**
     * <h3>AccessToken</h3>
     */
    private String accessToken;

    /**
     * <h3>过期时间</h3>
     */
    private Long expiresIn;

    /**
     * <h3>刷新Token</h3>
     */
    private String refreshToken;

    /**
     * <h3>权限范围</h3>
     */
    private String scope;
}
