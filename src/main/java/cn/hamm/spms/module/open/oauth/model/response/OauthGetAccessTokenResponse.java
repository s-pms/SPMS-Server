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
     * AccessToken
     */
    private String accessToken;

    /**
     * 过期时间(秒)
     */
    private Long expiresIn;

    /**
     * 刷新Token
     */
    private String refreshToken;

    /**
     * 权限范围
     */
    private String scope;
}
