package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.airpower.web.access.AccessConfig;
import cn.hamm.airpower.web.access.AccessTokenUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.user.enums.UserTokenType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN_EXIST;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class PersonalTokenService extends BaseService<PersonalTokenEntity, PersonalTokenRepository> {

    public static final String PERSONAL_TOKEN_NAME = "personal";

    @Autowired
    private AccessConfig accessConfig;

    /**
     * 根据令牌获取
     *
     * @param token 令牌
     * @return 令牌
     */
    public PersonalTokenEntity getByToken(String token) {
        return repository.getByToken(token);
    }

    @Override
    protected @NotNull PersonalTokenEntity beforeAdd(@NotNull PersonalTokenEntity personalToken) {
        List<PersonalTokenEntity> list = filter(new PersonalTokenEntity().setUser(personalToken.getUser()).setName(personalToken.getName()));
        FORBIDDEN_EXIST.when(!list.isEmpty(), "创建失败，该用户存在相同名称的私人令牌！");
        personalToken.setToken(createToken(personalToken.getUser().getId()));
        return personalToken;
    }

    @Override
    protected @NotNull PersonalTokenEntity beforeUpdate(@NotNull PersonalTokenEntity personalToken) {
        personalToken.setToken(null);
        return personalToken;
    }

    /**
     * 创建私人令牌
     *
     * @return AppKey
     */
    public final String createToken(long userId) {
        String token = AccessTokenUtil.create().setPayloadId(userId)
                .addPayload(UserTokenType.TYPE, UserTokenType.PERSONAL.getKey())
                .addPayload(PERSONAL_TOKEN_NAME, Math.random())
                .build(accessConfig.getAccessTokenSecret());
        PersonalTokenEntity openApp = getByToken(token);
        FORBIDDEN_EXIST.whenNotNull(openApp, "创建失败，私人令牌重复！");
        return token;
    }
}
