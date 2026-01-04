package cn.hamm.spms.module.open.oauth.model.base;

import cn.hamm.airpower.core.RootModel;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.module.open.oauth.IOauthAction;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>获取用户信息请求</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OauthAccessTokenRequest extends RootModel<OauthAccessTokenRequest> implements IOauthAction {
    @Description("AccessToken")
    @NotBlank(groups = {WhenAccessTokenRequired.class}, message = "AccessToken 不能为空")
    private String accessToken;

}
