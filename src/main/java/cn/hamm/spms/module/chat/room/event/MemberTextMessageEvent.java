package cn.hamm.spms.module.chat.room.event;

import cn.hamm.spms.module.chat.room.model.RoomMemberEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>房间成员文本消息事件</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class MemberTextMessageEvent extends RoomMemberEvent {
    /**
     * <h2>文本消息内容</h2>
     */
    private String text;
}
