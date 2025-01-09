package cn.hamm.spms.module.open.oauth.model.base;

import cn.hamm.airpower.root.RootModel;
import cn.hamm.spms.module.personnel.user.enums.UserGender;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>Oauth用户信息</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OauthUserInfo extends RootModel<OauthUserInfo> {
    private String userId;
    private String avatar;
    private String nickName;
    private UserGender gender;
}
