package cn.hamm.spms.module.system.config;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>系统配置</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface ConfigRepository extends BaseRepository<ConfigEntity> {
    /**
     * 按标识查询
     *
     * @param flag 配置标识
     * @return 配置信息，不存在时返回 {@code null}
     */
    ConfigEntity getByFlag(String flag);
}
