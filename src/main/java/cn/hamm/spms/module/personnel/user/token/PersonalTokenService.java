package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.airpower.util.AccessTokenUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_EXIST;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class PersonalTokenService extends BaseService<PersonalTokenEntity, PersonalTokenRepository> {

    public static final String PERSONAL_TOKEN_NAME = "name";

    /**
     * <h3>根据令牌获取</h3>
     *
     * @param token 令牌
     * @return 令牌
     */
    public PersonalTokenEntity getByToken(String token) {
        return repository.getByToken(token);
    }

    @Override
    protected @NotNull PersonalTokenEntity beforeAdd(@NotNull PersonalTokenEntity personalToken) {
        personalToken.setToken(createToken(personalToken.getUser().getId(), personalToken.getName()));
        return personalToken;
    }

    @Override
    protected @NotNull PersonalTokenEntity beforeUpdate(@NotNull PersonalTokenEntity personalToken) {
        personalToken.setToken(null);
        return personalToken;
    }

    /**
     * <h3>创建私人令牌</h3>
     *
     * @return AppKey
     */
    public final String createToken(long userId, String name) {
        String token = AccessTokenUtil.create().setPayloadId(userId).addPayload(PERSONAL_TOKEN_NAME, name).build(serviceConfig.getAccessTokenSecret());
        PersonalTokenEntity openApp = getByToken(token);
        FORBIDDEN_EXIST.whenNotNull(openApp, "创建失败，该用户存在相同名称的私人令牌！");
        List<PersonalTokenEntity> list = filter(new PersonalTokenEntity().setUser(new UserEntity().setId(userId)).setName(name));
        FORBIDDEN_EXIST.when(!list.isEmpty(), "创建失败，该用户存在相同名称的私人令牌！");
        return token;
    }
}
