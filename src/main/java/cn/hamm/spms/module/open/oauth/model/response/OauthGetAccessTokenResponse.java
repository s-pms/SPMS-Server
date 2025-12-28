package cn.hamm.spms.module.open.oauth.model.response;

import cn.hamm.airpower.meta.Meta;
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
    @Meta
    private String accessToken;

    /**
     * 过期时间(秒)
     */
    @Meta
    private Long expiresIn;

    /**
     * 刷新 Token
     */
    @Meta
    private String refreshToken;

    /**
     * 权限范围
     */
    @Meta
    private String scope;
}
