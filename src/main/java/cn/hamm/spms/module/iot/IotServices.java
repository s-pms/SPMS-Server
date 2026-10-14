package cn.hamm.spms.module.iot;

import cn.hamm.spms.module.iot.parameter.ParameterService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>物联网模块服务聚合</h1>
 *
 * @author Hamm.cn
 */
@Component
public class IotServices {

    @Getter
    private static ParameterService parameterService;

    @Autowired
    private void initService(
            ParameterService parameterService
    ) {
        IotServices.parameterService = parameterService;
    }
}
