package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.airpower.util.AccessTokenUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class PersonalTokenService extends BaseService<PersonalTokenEntity, PersonalTokenRepository> {
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
        personalToken.setToken(createToken(personalToken.getUser().getId()));
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
    public final String createToken(long userId) {
        String token = AccessTokenUtil.create().setPayloadId(userId).build(serviceConfig.getAccessTokenSecret());
        PersonalTokenEntity openApp = getByToken(token);
        if (Objects.isNull(openApp)) {
            return token;
        }
        return createToken(userId);
    }
}
