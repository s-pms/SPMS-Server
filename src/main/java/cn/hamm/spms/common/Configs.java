package cn.hamm.spms.common;

import cn.hamm.airpower.curd.config.CurdConfig;
import cn.hamm.airpower.websocket.WebSocketConfig;
import cn.hamm.spms.common.influx.InfluxConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>配置聚合</h1>
 *
 * @author Hamm.cn
 * @apiNote 把 Spring 托管的各份配置暴露成静态字段，供工具类、静态方法等拿不到容器的地方使用
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
