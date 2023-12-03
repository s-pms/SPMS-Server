package com.qqlab.spms.helper.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <h1>Websocket消息体</h1>
 *
 * @author hamm
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WebsocketMessage<T> {
    private String type;

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
