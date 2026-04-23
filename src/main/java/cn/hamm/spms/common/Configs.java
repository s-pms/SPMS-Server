package cn.hamm.spms.common;

import cn.hamm.airpower.curd.config.CurdConfig;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.common.config.InfluxConfig;
import cn.hamm.spms.common.config.WebSocketConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>配置整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class Configs {
    @Getter
    private static CurdConfig curdConfig;

    @Getter
    private static AppConfig appConfig;

    @Getter
    private static InfluxConfig influxConfig;

    @Getter
    private static WebSocketConfig webSocketConfig;

    @Autowired
    private void initService(
            CurdConfig curdConfig,
            AppConfig appConfig,
            InfluxConfig influxConfig,
            WebSocketConfig webSocketConfig
    ) {
        Configs.curdConfig = curdConfig;
        Configs.appConfig = appConfig;
        Configs.influxConfig = influxConfig;
        Configs.webSocketConfig = webSocketConfig;
    }
}
