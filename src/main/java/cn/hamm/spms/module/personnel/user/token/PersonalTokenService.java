package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.spms.base.BaseService;
import org.apache.commons.codec.digest.DigestUtils;
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
        personalToken.setToken(createToken());
        return personalToken;
    }

    @Override
    protected @NotNull PersonalTokenEntity beforeUpdate(@NotNull PersonalTokenEntity personalToken) {
        personalToken.setToken(null);
        return personalToken;
    }

    /**
     * <h3>创建AppKey</h3>
     *
     * @return AppKey
     */
    public final String createToken() {
        String appKey = DigestUtils.sha1Hex(RandomUtil.randomString());
        PersonalTokenEntity openApp = getByToken(appKey);
        if (Objects.isNull(openApp)) {
            return appKey;
        }
        return createToken();
    }
}
