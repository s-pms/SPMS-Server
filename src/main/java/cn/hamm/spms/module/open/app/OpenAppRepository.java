package cn.hamm.spms.module.open.app;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>开放应用</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface OpenAppRepository extends BaseRepository<OpenAppEntity> {
    /**
     * 通过 AppKey 查询应用
     *
     * @param appKey AppKey
     * @return 应用
     */
    OpenAppEntity getByAppKey(String appKey);
}
