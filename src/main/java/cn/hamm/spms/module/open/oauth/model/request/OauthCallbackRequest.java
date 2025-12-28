package cn.hamm.spms.module.open.oauth.model.request;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.module.open.oauth.model.base.OauthAppKeyRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>创建Code请求</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OauthCallbackRequest extends OauthAppKeyRequest {
    @Description("Code")
    @NotBlank(groups = {WhenOauthCallback.class}, message = "Code 不能为空")
    private String code;

    @Description("Platform")
    @NotBlank(groups = {WhenOauthCallback.class}, message = "Platform 不能为空")
    private String platform;

    public interface WhenOauthCallback {
    }
}
