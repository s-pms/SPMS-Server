package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.airpower.api.config.ApiConfig;
import cn.hamm.airpower.core.AccessTokenUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.user.enums.UserTokenType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_EXIST;

/**
 * <h1>私人令牌</h1>
 *
 * @author Hamm.cn
 */
@Service
public class PersonalTokenService extends BaseService<PersonalTokenEntity, PersonalTokenRepository> {

    /**
     * 私人令牌在 AccessToken 中的 payload 名
     */
    public static final String PERSONAL_TOKEN_NAME = "personal";

    @Autowired
    private ApiConfig apiConfig;

    /**
     * 按令牌查询
     *
     * @param token 令牌
     * @return 私人令牌，不存在时返回 {@code null}
     */
    public PersonalTokenEntity getByToken(String token) {
        return repository.getByToken(token);
    }

    @Override
    protected @NotNull PersonalTokenEntity beforeAppAdd(@NotNull PersonalTokenEntity personalToken) {
        List<PersonalTokenEntity> list = filter(new PersonalTokenEntity().setUser(personalToken.getUser()).setName(personalToken.getName()));
        FORBIDDEN_EXIST.when(!list.isEmpty(), "创建失败，该用户存在相同名称的私人令牌！");
        personalToken.setToken(createToken(personalToken.getUser().getId()));
        return personalToken;
    }

    @Override
    protected @NotNull PersonalTokenEntity beforeAppUpdate(@NotNull PersonalTokenEntity personalToken) {
        personalToken.setToken(null);
        return personalToken;
    }

    /**
     * 生成私人令牌
     *
     * @param userId 用户 ID
     * @return 令牌
     * @apiNote 同一用户重复生成时会撞出相同 token，靠创建后的存在性检查拒绝
     */
    public final String createToken(long userId) {
        String token = AccessTokenUtil.create().setPayloadId(userId)
                .addPayload(UserTokenType.TYPE, UserTokenType.PERSONAL.getKey())
                .addPayload(PERSONAL_TOKEN_NAME, Math.random())
                .build(apiConfig.getAccessTokenSecret());
        PersonalTokenEntity openApp = getByToken(token);
        FORBIDDEN_EXIST.whenNotNull(openApp, "创建失败，私人令牌重复！");
        return token;
    }
}
