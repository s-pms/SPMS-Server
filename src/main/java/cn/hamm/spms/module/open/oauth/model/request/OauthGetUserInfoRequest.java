package cn.hamm.spms.module.open.oauth.model.request;

import cn.hamm.spms.module.open.oauth.model.base.OauthAccessTokenRequest;
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
public class OauthGetUserInfoRequest extends OauthAccessTokenRequest {
}
