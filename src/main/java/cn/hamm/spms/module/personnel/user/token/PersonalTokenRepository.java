package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface PersonalTokenRepository extends BaseRepository<PersonalTokenEntity> {
    /**
     * <h3>通过令牌查询</h3>
     *
     * @param token 令牌
     * @return 应用
     */
    PersonalTokenEntity getByToken(String token);
}
