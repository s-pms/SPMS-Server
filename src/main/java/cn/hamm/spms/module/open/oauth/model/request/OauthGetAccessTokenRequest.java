package cn.hamm.spms.module.open.oauth.model.request;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.module.open.oauth.model.base.OauthAppKeyRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>创建 Code 请求</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OauthGetAccessTokenRequest extends OauthAppKeyRequest {
    @Description("Code")
    @NotBlank(groups = {WhenGetAccessToken.class}, message = "Code 不能为空")
    private String code;

    @Description("AppSecret")
    @NotBlank(groups = {WhenGetAccessToken.class}, message = "AppSecret 不能为空")
    private String appSecret;

    /**
     * Code 换 AccessToken
     */
    public interface WhenGetAccessToken {
    }
}
