package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>私人令牌</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PersonalTokenRepository extends BaseRepository<PersonalTokenEntity> {
    /**
     * 按令牌查询
     *
     * @param token 令牌
     * @return 私人令牌，不存在时返回 {@code null}
     */
    PersonalTokenEntity getByToken(String token);
}
