package cn.hamm.spms.common.helper.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <h1>Websocket消息体</h1>
 *
 * @author Hamm
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

    /**
     * <h2>设置消息类型</h2>
     *
     * @param type 消息类型
     * @return 消息对象
     */
    public WebsocketMessage<T> setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * <h2>设置消息体</h2>
     *
     * @param body 消息体
     * @return 消息对象
     */
    public WebsocketMessage<T> setBody(T body) {
        this.body = body;
        return this;
    }
}
