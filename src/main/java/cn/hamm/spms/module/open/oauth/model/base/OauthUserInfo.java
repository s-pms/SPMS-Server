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
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别
     */
    private UserGender gender;
}
