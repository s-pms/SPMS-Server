package cn.hamm.spms;

import cn.hamm.airpower.websocket.AbstractWebSocketConfigurer;
import cn.hamm.spms.common.AppWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;

/**
 * <h1>WebSocket配置</h1>
 *
 * @author Hamm.cn
 */
@Configuration
public class WebsocketConfigurer extends AbstractWebSocketConfigurer {
    @Autowired
    private AppWebSocketHandler appWebSocketHandler;

    @Override
    public WebSocketHandler getWebsocketHandler() {
        return appWebSocketHandler;
    }
}
