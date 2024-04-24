package cn.hamm.spms.common.helper.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <h1>Websocket消息体</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("unused")
@Data
@Accessors(chain = true)
public class WebsocketMessage<T> {
    /**
     * <h2>消息类型</h2>
     */
    private String type;

    /**
     * <h2>消息体</h2>
     */
    private T body;
}
