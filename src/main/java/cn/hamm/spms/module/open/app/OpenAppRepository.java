package cn.hamm.spms.module.open.app;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface OpenAppRepository extends BaseRepository<OpenAppEntity> {
    /**
     * <h2>通过AppKey查询应用</h2>
     *
     * @param appKey AppKey
     * @return 应用
     */
    OpenAppEntity getByAppKey(String appKey);
}
