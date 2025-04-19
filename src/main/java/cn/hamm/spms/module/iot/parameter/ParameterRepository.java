package cn.hamm.spms.module.iot.parameter;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface ParameterRepository extends BaseRepository<ParameterEntity> {
    /**
     * 根据参数编码查询
     *
     * @param code 参数编码
     * @return 参数
     */
    ParameterEntity getByCode(String code);
}
