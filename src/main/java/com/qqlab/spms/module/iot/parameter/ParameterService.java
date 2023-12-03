package com.qqlab.spms.module.iot.parameter;

import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class ParameterService extends BaseService<ParameterEntity, ParameterRepository> {
    /**
     * <h2>通过参数编码查询</h2>
     *
     * @param code 参数编码
     * @return 参数
     */
    public ParameterEntity getByCode(String code) {
        return repository.getByCode(code);
    }
}
