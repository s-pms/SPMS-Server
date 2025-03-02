package cn.hamm.spms.module.open.oauth.model.base;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootModel;
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
public class OauthAppKeyRequest extends RootModel<OauthAppKeyRequest> implements IOauthAction {
    @Description("AppKey")
    @NotBlank(groups = {WhenAppKeyRequired.class}, message = "AppKey不能为空")
    private String appKey;

}
