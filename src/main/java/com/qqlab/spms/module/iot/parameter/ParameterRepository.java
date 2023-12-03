package com.qqlab.spms.module.iot.parameter;

import com.qqlab.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface ParameterRepository extends BaseRepository<ParameterEntity> {
    /**
     * <h2>根据参数编码查询</h2>
     *
     * @param code 参数编码
     * @return 参数
     */
    ParameterEntity getByCode(String code);
}
