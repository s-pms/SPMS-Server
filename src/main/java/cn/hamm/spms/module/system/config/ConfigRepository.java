package cn.hamm.spms.module.system.config;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface ConfigRepository extends BaseRepository<ConfigEntity> {
    /**
     * 根据标识查询配置信息
     *
     * @param flag 配置标识
     * @return 配置信息
     */
    ConfigEntity getByFlag(String flag);
}
