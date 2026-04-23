package cn.hamm.spms.common.config;

import cn.hamm.airpower.websocket.WebSocketSupport;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static cn.hamm.airpower.websocket.WebSocketSupport.NO;

/**
 * <h1>WebSocket 配置</h1>
 * 覆盖 airpower-websocket 中的配置，修复属性绑定错误
 *
 * @author Hamm.cn
 */
@Data
@Configuration
@ConfigurationProperties("airpower.websocket")
public class WebSocketConfig {
    /**
     * PING
     */
    private String ping = "PING";

    /**
     * PONG
     */
    private String pong = "PONG";

    /**
     * WebSocket 路径
     */
    private String path = "/websocket";

    /**
     * WebSocket 支持方式
     */
    private WebSocketSupport support = NO;

    /**
     * 发布订阅的频道前缀
     */
    private String channelPrefix = "airpower:";

    /**
     * WebSocket 允许的跨域
     */
    private String allowedOrigins = "*";
}
