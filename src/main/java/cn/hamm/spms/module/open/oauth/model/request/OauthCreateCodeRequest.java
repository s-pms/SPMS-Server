package cn.hamm.spms.module.open.oauth.model.request;

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
public class OauthCreateCodeRequest extends OauthAppKeyRequest {

    @NotBlank(groups = {WhenCreateCode.class})
    private String scope;

    public interface WhenCreateCode {
    }
}
